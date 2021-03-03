package indi.atlantis.framework.tridenter.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.primitives.Floats;

/**
 * 
 * RequestStatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RequestStatisticIndicator extends AbstractStatisticIndicator implements RequestInterceptor, StatisticIndicator {

	private Float timeoutPercentage = 0.8F;
	private Float errorPercentage = 0.8F;
	private Float permitPercentage = 0.8F;

	public void setTimeoutPercentage(Float timeoutPercentage) {
		this.timeoutPercentage = timeoutPercentage;
	}

	public void setErrorPercentage(Float errorPercentage) {
		this.errorPercentage = errorPercentage;
	}

	public void setPermitPercentage(Float permitPercentage) {
		this.permitPercentage = permitPercentage;
	}

	@Override
	public boolean beforeSubmit(String provider, Request request) {
		boolean proceed = true;
		Statistic statistic = compute(provider, request);
		long totalCount = statistic.getTotalCount();
		if (timeoutPercentage != null && totalCount > 0) {
			long timeoutCount = statistic.getTimeoutCount();
			proceed &= Floats.toFixed((float) (timeoutCount / totalCount), 2) < timeoutPercentage.floatValue();
		}
		if (errorPercentage != null && totalCount > 0) {
			long failedCount = statistic.getFailedCount();
			proceed &= Floats.toFixed((float) (failedCount / totalCount), 2) < errorPercentage.floatValue();
		}
		if (permitPercentage != null && totalCount > 0) {
			long maxPermits = statistic.getPermit().getMaxPermits();
			long availablePermits = statistic.getPermit().getAvailablePermits();
			proceed &= Floats.toFixed((float) ((maxPermits - availablePermits) / maxPermits), 2) < permitPercentage.floatValue();
		}
		return proceed;
	}

	@Override
	public void afterSubmit(String provider, Request request, Object responseEntity, Throwable e) {
		Statistic statistic = compute(provider, request);
		long elapsed = System.currentTimeMillis() - request.getTimestamp();
		statistic.setElapsed(elapsed);
		if (responseEntity != null && (responseEntity instanceof ResponseEntity)
				&& !((ResponseEntity<?>) responseEntity).getStatusCode().is2xxSuccessful()) {
			statistic.failure.incrementAndGet();
		} else if (e != null && e instanceof RestClientException) {
			if (isRequestTimeout((RestClientException) e)) {
				statistic.timeout.incrementAndGet();
			} else {
				statistic.failure.incrementAndGet();
			}
		}
		statistic.total.incrementAndGet();
		statistic.qps.incrementAndGet();
	}

	private boolean isRequestTimeout(RestClientException e) {
		return e instanceof RestfulException && ((RestfulException) e).getInterruptedType() == InterruptedType.REQUEST_TIMEOUT;
	}

}
