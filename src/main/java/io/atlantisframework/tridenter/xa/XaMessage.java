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
package io.atlantisframework.tridenter.xa;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.devtools.comparator.ComparatorHelper;

import io.atlantisframework.tridenter.ApplicationInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * XaMessage
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Getter
@Setter
public class XaMessage implements Comparable<XaMessage> {

	private String xaId;
	private long serial;
	private MethodInfo methodInfo;
	private XaState state;
	private long timeout;
	private boolean completed;
	private ApplicationInfo applicationInfo;
	private long timestamp;

	public XaMessage(String xaId, long serial, MethodInfo methodInfo, XaState state, long timeout, boolean completed,
			ApplicationInfo applicationInfo) {
		this.xaId = xaId;
		this.serial = serial;
		this.methodInfo = methodInfo;
		this.state = state;
		this.timeout = timeout;
		this.completed = completed;
		this.applicationInfo = applicationInfo;
		this.timestamp = System.currentTimeMillis();
	}

	public XaMessage() {
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(XaMessage other) {
		if (getXaId().equals(other.getXaId())) {
			return ComparatorHelper.compareTo(getSerial(), other.getSerial());
		}
		return 0;
	}

}
