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
package indi.atlantis.framework.tridenter.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 
 * ResponseStatisticIndicator
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ResponseStatisticIndicator extends AbstractStatisticIndicator implements HandlerInterceptor, StatisticIndicator {

	private static final String REQUEST_ATTRIBUTE_TIMESTAMP = "timestamp";

	@Value("${spring.application.name}")
	private String applicationName;

	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
		req.setAttribute(REQUEST_ATTRIBUTE_TIMESTAMP, System.currentTimeMillis());
		Statistic statistic = compute(applicationName, SimpleRequest.of(req.getServletPath()));
		statistic.getPermit().accquire();
		return true;
	}

	public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, @Nullable Exception ex) throws Exception {
		Request request = SimpleRequest.of(req.getServletPath());
		Statistic statistic = compute(applicationName, request);
		statistic.getPermit().release();

		long startTime = (Long) req.getAttribute(REQUEST_ATTRIBUTE_TIMESTAMP);
		long elapsed = System.currentTimeMillis() - startTime;
		statistic.setElapsed(elapsed);

		statistic.total.incrementAndGet();
		statistic.qps.incrementAndGet();
		if (ex != null) {
			statistic.failure.incrementAndGet();
		}
	}

}
