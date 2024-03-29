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
package io.atlantisframework.tridenter.multiprocess;

import java.util.Collection;

/**
 * 
 * DefaultParallelization
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class DefaultParallelization implements Parallelization {

	@Override
	public Object[] slice(Object argument) {
		if (argument instanceof CharSequence) {
			return ((CharSequence) argument).toString().split(",");
		} else if (argument instanceof Object[]) {
			return (Object[]) argument;
		} else if (argument instanceof Collection<?>) {
			return ((Collection<?>) argument).toArray();
		}
		return new Object[] { argument };
	}

	@Override
	public Object merge(Object[] results) {
		return results;
	}

}
