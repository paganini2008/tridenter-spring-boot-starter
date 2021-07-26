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
package indi.atlantis.framework.tridenter.gateway;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.io.PathUtils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * Router
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Accessors(fluent = true)
@Data
public final class Router implements Comparable<Router> {

	private static final String PATTERN = "[_0-9a-zA-Z\\*\\/]+";

	private final String prefix;
	private final int prefixEndPosition;
	private String provider = "";
	private int retries;
	private int timeout = Integer.MAX_VALUE;
	private int allowedPermits = Integer.MAX_VALUE;
	private boolean cached;
	private String url;
	private ResourceType resourceType = ResourceType.DEFAULT;
	private Charset charset = CharsetUtils.UTF_8;
	private Class<?> fallback;

	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();
	private final List<String> ignoredHeaders = new ArrayList<String>();

	Router(String prefix) {
		if (prefix.endsWith("/")) {
			throw new IllegalArgumentException("The prefix of router must not end with '/'");
		}
		if (!prefix.matches(PATTERN)) {
			throw new IllegalArgumentException("The prefix of router contains illegal characters. Input string: " + prefix);
		}
		this.prefix = prefix;
		this.prefixEndPosition = PathUtils.indexOfLastSeparator(prefix);
	}

	public Router ignoredHeaders(String[] headerNames) {
		this.ignoredHeaders.addAll(Arrays.asList(headerNames));
		return this;
	}

	public Router defaultHeaders(String[] nameValues) {
		Map<String, String> headerMap = MapUtils.toMap(nameValues);
		for (Map.Entry<String, String> entry : headerMap.entrySet()) {
			this.defaultHeaders.add(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public int compareTo(Router other) {
		return other.prefixEndPosition() - prefixEndPosition();
	}

	public String trimPath(String path) {
		return prefixEndPosition >= 0 ? path.substring(prefixEndPosition) : path;
	}

	public static enum ResourceType {

		DEFAULT, REDIRECT, STREAM, FILE

	}

}
