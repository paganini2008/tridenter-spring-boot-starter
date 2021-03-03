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
 * @author Jimmy Hoff
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
