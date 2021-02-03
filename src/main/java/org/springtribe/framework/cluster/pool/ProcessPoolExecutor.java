package org.springtribe.framework.cluster.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;
import org.springtribe.framework.reditools.common.SharedLatch;
import org.springtribe.framework.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolExecutor
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ProcessPoolExecutor implements ProcessPool {

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.cluster.pool.latch.timeout:-1}")
	private int timeout;

	@Autowired
	private SharedLatch sharedLatch;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private DelayQueue delayQueue;

	private final AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void execute(Invocation invocation) {
		checkIfRunning();
		boolean acquired = timeout > 0 ? sharedLatch.acquire(timeout, TimeUnit.SECONDS) : sharedLatch.acquire();
		if (acquired) {
			if (log.isTraceEnabled()) {
				log.trace("Now processPool's concurrency is " + sharedLatch.cons());
			}
			applicationMulticastGroup.unicast(applicationName, ProcessPoolTaskListener.class.getName(), invocation);
		} else {
			delayQueue.offer(invocation);
			log.info("Invocation: {} go into the pending queue.", invocation);
		}

	}

	@Override
	public TaskPromise submit(Invocation invocation) {
		checkIfRunning();
		boolean acquired = timeout > 0 ? sharedLatch.acquire(timeout, TimeUnit.SECONDS) : sharedLatch.acquire();
		if (acquired) {
			if (log.isTraceEnabled()) {
				log.trace("Now processPool's concurrency is " + sharedLatch.cons());
			}
			applicationMulticastGroup.unicast(applicationName, ProcessPoolTaskListener.class.getName(), invocation);
		} else {
			delayQueue.offer(invocation);
			log.info("Invocation: {} go into the pending queue.", invocation);
		}
		ProcessPoolTaskPromise promise = new ProcessPoolTaskPromise(invocation.getId());
		redisMessageSender.subscribeChannel(invocation.getId(), promise);
		return promise;
	}

	private void checkIfRunning() {
		if (!running.get()) {
			throw new IllegalStateException("ProcessPool is shutdown now.");
		}
	}

	@Override
	public int getQueueSize() {
		return delayQueue.size();
	}

	@Override
	public void shutdown() {
		if (!running.get()) {
			return;
		}
		running.set(false);
		delayQueue.waitForTermination();
		sharedLatch.join();
	}

}
