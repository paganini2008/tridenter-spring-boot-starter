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

/**
 * 
 * XaStateChangeListener
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public interface XaStateChangeListener {

	default void prepare(XaMessage event) throws Exception {
	}

	default void commit(XaMessage event) throws Exception {
	}

	default void rollback(XaMessage event) throws Exception {
	}

	default void rollback(XaMessage event, Exception e) throws Exception {
	}

	default void finish(XaMessage event) throws Exception {
	}

}
