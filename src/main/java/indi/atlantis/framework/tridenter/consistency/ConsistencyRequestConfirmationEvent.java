/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.consistency;

import org.springframework.context.ApplicationEvent;

import indi.atlantis.framework.tridenter.ApplicationInfo;

/**
 * 
 * ConsistencyRequestConfirmationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestConfirmationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4041272418956233610L;

	public ConsistencyRequestConfirmationEvent(ConsistencyRequest request, ApplicationInfo applicationInfo, boolean ok) {
		super(request.getValue());
		this.request = request;
		this.applicationInfo = applicationInfo;
		this.ok = ok;
	}

	private final ConsistencyRequest request;
	private final ApplicationInfo applicationInfo;
	private final boolean ok;

	public ConsistencyRequest getRequest() {
		return request;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public boolean isOk() {
		return ok;
	}

}
