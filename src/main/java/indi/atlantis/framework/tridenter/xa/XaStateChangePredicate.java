/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.xa;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaStateChangePredicate
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Slf4j
public class XaStateChangePredicate implements XaMessageAckPredicate {

	private final List<XaStateChangeListener> listeners = new CopyOnWriteArrayList<XaStateChangeListener>();

	@Override
	public void addListener(XaStateChangeListener... listeners) {
		if (listeners != null) {
			this.listeners.addAll(Arrays.asList(listeners));
		}
	}

	@Override
	public void removeListener(XaStateChangeListener listener) {
		if (listener != null) {
			while (listeners.contains(listener)) {
				this.listeners.remove(listener);
			}
		}
	}

	@Override
	public boolean test(XaMessageAck messageAck) {
		for (XaStateChangeListener listener : listeners) {
			if (messageAck.getNextState() != null) {
				XaMessage message = messageAck.getMessage();
				try {
					switch (messageAck.getNextState()) {
					case PREPARED:
						listener.prepare(message);
						break;
					case COMMIT:
						listener.commit(message);
						break;
					case ROLLBACK:
						listener.rollback(message);
						break;
					case FINISHED:
						listener.finish(message);
						break;
					default:
						break;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					try {
						switch (messageAck.getNextState()) {
						case COMMIT:
							listener.rollback(message, e);
							break;
						case ROLLBACK:
							listener.rollback(message, e);
							break;
						default:
							break;
						}
					} catch (Exception e1) {
						log.error(e1.getMessage(), e1);
					}
					return false;
				}
			}
		}
		return true;
	}

}
