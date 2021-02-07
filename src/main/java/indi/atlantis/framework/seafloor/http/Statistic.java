package indi.atlantis.framework.seafloor.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.paganini2008.devtools.collection.LruList;
import com.github.paganini2008.devtools.multithreads.AtomicLongSequence;

/**
 * 
 * Statistic
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public final class Statistic {

	private final String provider;
	private final String path;
	private final AtomicLongSequence totalExecution = new AtomicLongSequence();
	private final AtomicLongSequence timeoutExecution = new AtomicLongSequence();
	private final AtomicLongSequence failedExecution = new AtomicLongSequence();
	private final Permit permit;
	private final Snapshot snapshot;

	public Statistic(String provider, String path, int maxPermits) {
		this.provider = provider;
		this.path = path;
		this.permit = new Permit(maxPermits);
		this.snapshot = new Snapshot(totalExecution);
	}

	private volatile long lastExecutionCount;
	private long qps;

	public String getProvider() {
		return provider;
	}

	public String getPath() {
		return path;
	}

	@JsonIgnore
	public AtomicLongSequence getTotalExecution() {
		return totalExecution;
	}

	@JsonIgnore
	public AtomicLongSequence getTimeoutExecution() {
		return timeoutExecution;
	}

	@JsonIgnore
	public AtomicLongSequence getFailedExecution() {
		return failedExecution;
	}

	public long getTotalExecutionCount() {
		return totalExecution.get();
	}

	public long getTimeoutExecutionCount() {
		return timeoutExecution.get();
	}

	public long getFailedExecutionCount() {
		return failedExecution.get();
	}

	public void calculateQps() {
		long totalExecutionCount = getTotalExecutionCount();
		this.qps = totalExecutionCount - lastExecutionCount;
		this.lastExecutionCount = totalExecutionCount;
	}

	public long getQps() {
		return qps;
	}

	public Permit getPermit() {
		return permit;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("provider", provider);
		data.put("path", path);
		data.put("totalExecutionCount", getTotalExecutionCount());
		data.put("timeoutExecutionCount", getTimeoutExecutionCount());
		data.put("failedExecutionCount", getFailedExecutionCount());
		data.put("qps", getQps());
		data.put("maximumRequestTime", snapshot.getMaximumRequestTime());
		data.put("minimumRequestTime", snapshot.getMinimumRequestTime());
		data.put("averageRequestTime", snapshot.getAverageRequestTime());
		data.put("activePermits", permit.getMaxPermits() - permit.getAvailablePermits());
		data.put("maxPermits", permit.getMaxPermits());
		return data;
	}

	public static class Permit {

		private final AtomicInteger counter;
		private final int maxPermits;

		Permit(int maxPermits) {
			this.counter = new AtomicInteger(0);
			this.maxPermits = maxPermits;
		}

		public int accquire() {
			return counter.incrementAndGet();
		}

		public int accquire(int permits) {
			return counter.addAndGet(permits);
		}

		public int release() {
			return counter.decrementAndGet();
		}

		public int release(int permits) {
			return counter.addAndGet(-permits);
		}

		public int getAvailablePermits() {
			return maxPermits - counter.get();
		}

		public int getMaxPermits() {
			return maxPermits;
		}

	}

	public static class Snapshot {

		private final List<Request> latestRequests = new LruList<Request>(120);
		private final AtomicLongSequence totalRequestTime = new AtomicLongSequence();
		private volatile long maximumRequestTime;
		private volatile long minimumRequestTime = Long.MAX_VALUE;

		Snapshot(AtomicLongSequence totalExecution) {
			this.totalExecution = totalExecution;
		}

		private final AtomicLongSequence totalExecution;

		public long addRequest(Request request) {
			return addRequest(request, request.getTimestamp());
		}

		public long addRequest(Request request, long startTime) {
			latestRequests.add(request);
			totalExecution.incrementAndGet();
			long elapsed = System.currentTimeMillis() - startTime;
			totalRequestTime.addAndGet(elapsed);
			maximumRequestTime = Long.max(maximumRequestTime, elapsed);
			minimumRequestTime = Long.min(minimumRequestTime, elapsed);
			return elapsed;
		}

		public long getMinimumRequestTime() {
			return minimumRequestTime;
		}

		public long getMaximumRequestTime() {
			return maximumRequestTime;
		}

		public long getAverageRequestTime() {
			return totalExecution.get() > 0 ? totalRequestTime.get() / totalExecution.get() : 0L;
		}

		public List<Request> getLatestRequests() {
			return new ArrayList<Request>(latestRequests);
		}

	}

}
