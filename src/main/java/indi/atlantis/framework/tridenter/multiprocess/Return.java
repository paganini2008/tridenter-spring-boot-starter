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

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Return
 * 
 * @author Fred Feng
 *
 * @version 1.0
 */
@Getter
@Setter
public class Return implements Serializable {

	private static final long serialVersionUID = 5736144131241770067L;
	private Invocation invocation;
	private Object returnValue;

	public Return() {
	}

	Return(Invocation invocation, Object returnValue) {
		this.invocation = invocation;
		this.returnValue = returnValue;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
