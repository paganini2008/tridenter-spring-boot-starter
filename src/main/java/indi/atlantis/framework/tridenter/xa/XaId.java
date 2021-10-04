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

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.web.servlet.HandlerInterceptor;

import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * UuidXaIdGenerator
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public class XaId implements HandlerInterceptor {
	
	static final String INDENTIFIER = "XA-ID";
	private static final NamedInheritableThreadLocal<String> threadLocal = new NamedInheritableThreadLocal<String>(INDENTIFIER);

	public static boolean has() {
		return StringUtils.isNotBlank(threadLocal.get());
	}

	public static String get() {
		String xaId = threadLocal.get();
		if (StringUtils.isBlank(xaId)) {
			threadLocal.set(UUID.randomUUID().toString());
		}
		return threadLocal.get();
	}

	public static void reset() {
		threadLocal.remove();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String transactionId = request.getHeader(INDENTIFIER);
		if (StringUtils.isNotBlank(transactionId)) {
			threadLocal.set(transactionId);
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		reset();
	}

}
