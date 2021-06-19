/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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

import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * ConsistencyRequestSerialCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestSerialCache {

	private final MultiMappedMap<String, Long, Long> serials = new MultiMappedMap<String, Long, Long>();
	private final MultiMappedMap<String, Long, Object> values = new MultiMappedMap<String, Long, Object>();

	public Object setValue(String name, long round, long serial, Object value) {
		serials.put(name, round, serial);
		if (value != null) {
			return values.put(name, round, value);
		}
		return null;
	}

	public long getSerial(String name, long round) {
		return serials.get(name, round, 0L);
	}

	public void clean(String name) {
		serials.remove(name);
		values.remove(name);
	}

}
