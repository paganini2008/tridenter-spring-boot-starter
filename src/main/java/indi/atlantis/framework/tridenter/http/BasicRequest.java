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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * 
 * BasicRequest
 *
 * @author Fred Feng
 * @version 1.0
 */
public class BasicRequest implements Request {

	private final Map<String, Object> attributeMap = new HashMap<String, Object>();
	private final String path;
	private final HttpMethod method;
	private final HttpHeaders headers;
	private final long timestamp;

	public BasicRequest(String path, HttpMethod method, HttpHeaders headers) {
		this.path = path;
		this.method = method;
		this.headers = headers;
		this.timestamp = System.currentTimeMillis();
	}

	private HttpEntity<Object> body;

	public HttpEntity<Object> getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = new HttpEntity<Object>(body, headers);
	}

	public void setBody(HttpEntity<Object> body) {
		this.body = body;
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			attributeMap.put(attributeName, attributeValue);
		} else {
			attributeMap.remove(attributeName);
		}
	}

	public Object getAttribute(String attributeName) {
		return getAttribute(attributeName, null);
	}

	public Object getAttribute(String attributeName, Object defaultValue) {
		return attributeMap.getOrDefault(attributeName, defaultValue);
	}

	public Map<String, Object> copyAttributes() {
		return Collections.unmodifiableMap(attributeMap);
	}

	public void clearAttributes() {
		attributeMap.clear();
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return method.name().toUpperCase() + " " + path;
	}

}
