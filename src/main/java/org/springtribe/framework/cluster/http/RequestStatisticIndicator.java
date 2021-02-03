package org.springtribe.framework.cluster.http;

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
		long totalExecutionCount = statistic.getTotalExecutionCount();
		if (timeoutPercentage != null && totalExecutionCount > 0) {
			long timeoutExecutionCount = statistic.getTimeoutExecutionCount();
			proceed &= Floats.toFixed((float) (timeoutExecutionCount / totalExecutionCount), 2) < timeoutPercentage.floatValue();
		}
		if (errorPercentage != null && totalExecutionCount > 0) {
			long failedExecutionCount = statistic.getFailedExecutionCount();
			proceed &= Floats.toFixed((float) (failedExecutionCount / totalExecutionCount), 2) < errorPercentage.floatValue();
		}
		if (permitPercentage != null && totalExecutionCount > 0) {
			long maxPermits = statistic.getPermit().getMaxPermits();
			long availablePermits = statistic.getPermit().getAvailablePermits();
			proceed &= Floats.toFixed((float) ((maxPermits - availablePermits) / maxPermits), 2) < permitPercentage.floatValue();
		}
		return proceed;
	}

	@Override
	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable e) {
		Statistic statistic = compute(provider, request);
		statistic.getSnapshot().addRequest(request);
		if (responseEntity != null && (responseEntity.getStatusCodeValue() < 200 || responseEntity.getStatusCodeValue() >= 300)) {
			statistic.getFailedExecution().incrementAndGet();
		} else if (e != null && e instanceof RestClientException) {
			if (isRequestTimeout((RestClientException) e)) {
				statistic.getTimeoutExecution().incrementAndGet();
			} else {
				statistic.getFailedExecution().incrementAndGet();
			}
		}
	}

	private boolean isRequestTimeout(RestClientException e) {
		return e instanceof RestfulException && ((RestfulException) e).getInterruptedType() == InterruptedType.REQUEST_TIMEOUT;
	}

}
