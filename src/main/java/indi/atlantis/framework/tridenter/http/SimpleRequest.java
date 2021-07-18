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
package indi.atlantis.framework.tridenter.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * SimpleRequest
 *
 * @author Fred Feng
 * @version 1.0
 */
public class SimpleRequest implements Request {

	private static final LruMap<String, SimpleRequest> cache = new LruMap<String, SimpleRequest>();

	private final String path;

	SimpleRequest(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public HttpMethod getMethod() {
		throw new UnsupportedOperationException("getMethod");
	}

	@Override
	public HttpHeaders getHeaders() {
		throw new UnsupportedOperationException("getHeaders");
	}

	@Override
	public HttpEntity<Object> getBody() {
		throw new UnsupportedOperationException("getBody");
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException("getTimestamp");
	}
	

	public static Request of(final String path) {
		return MapUtils.get(cache, path, () -> {
			return new SimpleRequest(path);
		});
	}

}
