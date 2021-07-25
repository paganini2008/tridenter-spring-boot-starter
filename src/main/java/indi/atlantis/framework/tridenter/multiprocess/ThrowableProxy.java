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
package indi.atlantis.framework.tridenter.multiprocess;

import java.io.Serializable;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.io.IOUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ThrowableProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class ThrowableProxy implements Serializable {

	private static final long serialVersionUID = 9079267952341059883L;
	private String msg;
	private String className;
	private String[] stackTrace;

	public ThrowableProxy() {
	}

	public ThrowableProxy(String msg, Throwable e) {
		this.msg = msg;
		this.className = e.getClass().getName();
		this.stackTrace = ExceptionUtils.toArray(e);
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Caused by: ");
		for (String trace : stackTrace) {
			str.append(trace).append(IOUtils.NEWLINE);
		}
		return str.toString();
	}

}
