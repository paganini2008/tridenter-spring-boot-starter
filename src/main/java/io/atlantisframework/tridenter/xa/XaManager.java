/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.xa;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MultiSetMap;

import io.atlantisframework.tridenter.LeaderState;
import io.atlantisframework.tridenter.election.ApplicationClusterLeaderEvent;
import io.atlantisframework.tridenter.utils.BeanLifeCycle;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaManager
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Slf4j
public class XaManager implements XaMessageListener, ApplicationListener<ApplicationClusterLeaderEvent>, BeanLifeCycle {

	@Autowired
	private XaMessageSender messageSender;

	private final ConcurrentMap<String, Boolean> booleanQueue = new ConcurrentHashMap<>();
	private final MultiSetMap<String, XaMessage> checkInQueue = new MultiSetMap<>();
	private final MultiSetMap<String, XaMessage> checkOutQueue = new MultiSetMap<>();
	private final MultiSetMap<String, XaMessage> preparedQueue = new MultiSetMap<>();
	private final MultiSetMap<String, XaMessage> committedQueue = new MultiSetMap<>();
	private final MultiSetMap<String, XaMessage> compensationQueue = new MultiSetMap<>();
	private final LruMap<String, XaMessage> historyQueue = new LruMap<>(256);

	private ScheduledFuture<?> timeoutCancellableFuture;
	private ScheduledFuture<?> rollbackCompensationFuture;

	@Qualifier("applicationClusterTaskScheduler")
	@Autowired
	private TaskScheduler taskScheduler;

	@Override
	public void configure() throws Exception {
		this.timeoutCancellableFuture = taskScheduler.scheduleWithFixedDelay(new TimeoutCancellableTask(), Duration.ofSeconds(60L));
		this.rollbackCompensationFuture = taskScheduler.scheduleWithFixedDelay(new RollbackCompensationTask(), Duration.ofSeconds(5L));
	}

	@Override
	public String getChannel() {
		return XaConstants.MESSAGE_CHANNEL_TRANSACTION_MANAGER;
	}

	public String[] getRunningXaIds() {
		return booleanQueue.keySet().toArray(new String[0]);
	}

	public boolean isRunning(String xaId) {
		return booleanQueue.containsKey(xaId);
	}

	public List<XaMessage> getUnfinishedTransactions() {
		return new ArrayList<XaMessage>(checkInQueue.toSingleValueMap().values());
	}

	public List<XaMessage> getFinishedTransactions() {
		return new ArrayList<XaMessage>(historyQueue.values());
	}

	protected XaMessage clean(String xaId) {
		booleanQueue.remove(xaId);
		compensationQueue.remove(xaId);
		committedQueue.remove(xaId);
		checkOutQueue.remove(xaId);
		NavigableSet<XaMessage> set = checkInQueue.remove(xaId);
		XaMessage first = CollectionUtils.getFirst(set);
		if (first != null) {
			historyQueue.put(xaId, first);
			if (log.isTraceEnabled()) {
				log.trace("[XA:{}:{}] is finished.", first.getXaId(), first.getSerial());
			}
		}
		return first;
	}

	@Override
	public void destroy() {
		if (timeoutCancellableFuture != null) {
			timeoutCancellableFuture.cancel(true);
		}
		if (rollbackCompensationFuture != null) {
			rollbackCompensationFuture.cancel(true);
		}
	}

	@Override
	public void onMessage(Object message) throws Exception {
		final XaMessage event = (XaMessage) message;
		final String xaId = event.getXaId();
		booleanQueue.put(xaId, booleanQueue.getOrDefault(xaId, true) && event.isCompleted());
		XaMessage last = null;
		switch (event.getState()) {
		case CHECK_IN:
			checkInQueue.add(xaId, event);
			messageSender.ack(event, null);
			break;
		case CHECK_OUT:
			checkOutQueue.add(xaId, event);
			messageSender.ack(event, null);
			if (booleanQueue.get(xaId)) {
				if (checkOutQueue.size(xaId) == checkInQueue.size(xaId)) {
					for (XaMessage e : checkOutQueue.get(xaId)) {
						messageSender.ack(e, XaState.PREPARED);
					}
				}
			} else {
				if (checkOutQueue.size(xaId) == checkInQueue.size(xaId)) {
					preparedQueue.addAll(xaId, checkOutQueue.get(xaId));
					last = preparedQueue.pollLast(xaId);
					messageSender.ack(last, XaState.ROLLBACK);
				}
			}
			break;
		case PREPARED:
			preparedQueue.add(xaId, event);
			if (preparedQueue.size(xaId) == checkOutQueue.size(xaId)) {
				if (booleanQueue.get(xaId)) {
					last = preparedQueue.pollLast(xaId);
					messageSender.ack(last, XaState.COMMIT);
				} else {
					last = preparedQueue.pollLast(xaId);
					messageSender.ack(last, XaState.ROLLBACK);
				}
			}
			break;
		case COMMIT:
			if (booleanQueue.get(xaId)) {
				committedQueue.add(xaId, event);
				last = preparedQueue.pollLast(xaId);
				if (last != null) {
					messageSender.ack(last, XaState.COMMIT);
				} else {
					for (XaMessage e : committedQueue.get(xaId)) {
						messageSender.ack(e, XaState.FINISHED);
					}
					clean(xaId);
				}
			} else {
				last = committedQueue.pollLast(xaId);
				if (last == null) {
					last = preparedQueue.pollLast(xaId);
				}
				if (last != null) {
					messageSender.ack(last, XaState.ROLLBACK);
				}
			}
			break;
		case ROLLBACK:
			if (event.isCompleted()) {
				last = committedQueue.pollLast(xaId);
				if (last == null) {
					last = preparedQueue.pollLast(xaId);
				}
				if (last != null) {
					messageSender.ack(last, XaState.ROLLBACK);
				} else {
					for (XaMessage e : checkOutQueue.get(xaId)) {
						e.setState(XaState.ROLLBACK);
						messageSender.ack(e, XaState.FINISHED);
					}
					clean(xaId);
				}
			} else {
				compensationQueue.add(xaId, event);
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderEvent event) {
		if (event.getLeaderState() == LeaderState.UP) {
			messageSender.subscribeXaMessages(this);
			log.info("XaManager is ready.");
		}
	}

	/**
	 * 
	 * TimeoutCancellableTask
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.4
	 */
	private class TimeoutCancellableTask implements Runnable {

		@Override
		public void run() {
			if (booleanQueue.isEmpty()) {
				return;
			}
			booleanQueue.keySet().forEach(xaId -> {
				NavigableSet<XaMessage> set = checkInQueue.get(xaId);
				if (set != null) {
					XaMessage first = set.first();
					if (first.getTimeout() > 0 && System.currentTimeMillis() - first.getTimestamp() > first.getTimeout()) {
						preparedQueue.addAll(xaId, checkOutQueue.get(xaId));
						XaMessage last = preparedQueue.pollLast(xaId);
						messageSender.ack(last, XaState.ROLLBACK);
					}
				}
			});
		}

	}

	/**
	 * 
	 * RollbackCompensationTask
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.4
	 */
	private class RollbackCompensationTask implements Runnable {

		@Override
		public void run() {
			if (compensationQueue.isEmpty()) {
				return;
			}
			compensationQueue.keySet().forEach(xaId -> {
				XaMessage last = compensationQueue.pollLast(xaId);
				if (last != null) {
					messageSender.ack(last, XaState.ROLLBACK);
				}
			});

		}
	}

}
