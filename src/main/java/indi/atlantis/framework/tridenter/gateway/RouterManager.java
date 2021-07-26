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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.github.paganini2008.devtools.collection.KeyMatchedMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RouterManager
 *
 * @author Fred Feng
 * 
 * @since 2.0.1
 */
@Slf4j
public final class RouterManager extends KeyMatchedMap<String, Router> implements ApplicationListener<ContextRefreshedEvent> {

	private static final long serialVersionUID = -1981160524314626755L;

	public RouterManager() {
		super(new ConcurrentHashMap<String, Router>(), false);
	}

	private final PathMatcher pathMatcher = new AntPathMatcher();

	@Autowired
	private RouterCustomizer routerCustomizer;

	public Router route(String prefix) {
		put(prefix, new Router(prefix));
		return match(prefix);
	}

	public Router match(String path) {
		return get(path);
	}

	@Override
	protected boolean match(String pattern, Object inputKey) {
		return pathMatcher.match(pattern, (String) inputKey);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		routerCustomizer.customize(this);
		for (Map.Entry<String, Router> entry : entrySet()) {
			log.info("[{}] route to {}", entry.getKey(), entry.getValue());
		}
	}

}
