package org.springtribe.framework.cluster.consistency;

import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * ConsistencyRequestSerialCache
 *
 * @author Jimmy Hoff
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
