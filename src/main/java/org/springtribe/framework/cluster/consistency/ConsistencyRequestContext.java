package org.springtribe.framework.cluster.consistency;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springtribe.framework.cluster.InstanceId;
import org.springtribe.framework.cluster.consistency.Court.Proposal;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.ClockTask;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestContext
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public final class ConsistencyRequestContext {

	public static final int CONSISTENCY_REQUEST_MAX_TIMEOUT = 60;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private Clock clock;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerial requestSerial;

	@Value("${spring.application.cluster.consistency.responseWaitingTime:1}")
	private long responseWaitingTime;

	@Autowired
	private Court court;

	public boolean propose(String name, Object value, int timeout) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Proposal name is a must.");
		}
		if (timeout > CONSISTENCY_REQUEST_MAX_TIMEOUT) {
			throw new IllegalArgumentException("Maximum timout is " + CONSISTENCY_REQUEST_MAX_TIMEOUT);
		}
		if (court.hasProposal(name)) {
			if (log.isTraceEnabled()) {
				log.trace("The proposal named '{}' is being processing currently. Please submit again after completion.", name);
			}
			return false;
		}
		Proposal proposal = new Proposal(name, value);
		if (!court.saveProposal(proposal)) {
			if (log.isTraceEnabled()) {
				log.trace("The proposal named '{}' is being processing currently. Please submit again after completion.", name);
			}
			return false;
		}
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(instanceId.getApplicationInfo()).setName(name).setRound(round).setSerial(serial)
				.setTimeout(timeout);
		multicastGroup.multicast(ConsistencyRequest.PREPARATION_OPERATION_REQUEST, request);

		ConsistencyRequestPreparationFuture preparationFuture = new ConsistencyRequestPreparationFuture(request);
		clock.schedule(preparationFuture, responseWaitingTime, TimeUnit.SECONDS);
		court.formulate(name, preparationFuture);
		return true;
	}

	public void sync(String anotherInstanceId, String name, Object value) {
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(instanceId.getApplicationInfo()).setName(name).setValue(value).setRound(round)
				.setSerial(serial).setTimeout(0);
		multicastGroup.send(anotherInstanceId, ConsistencyRequest.LEARNING_OPERATION_REQUEST, request);
	}

	/**
	 * 
	 * ConsistencyRequestCommitmentFuture
	 *
	 * @author Jimmy Hoff
	 * @since 1.0
	 */
	private class ConsistencyRequestCommitmentFuture extends ClockTask implements Formulation {

		private final ConsistencyRequest request;

		ConsistencyRequestCommitmentFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		public void directRun() {
			runTask();
		}

		@Override
		public String getPeriod() {
			return COMMITMENT_PERIOD;
		}

		@Override
		public boolean cancel() {
			return super.cancel();
		}

		@Override
		protected void runTask() {
			final String name = request.getName();
			Proposal proposal = court.getProposal(name);
			List<ConsistencyResponse> original = proposal != null ? proposal.getCommitments() : null;
			List<ConsistencyResponse> expected = proposal != null ? proposal.getPreparations() : null;
			int originalLength = original != null ? original.size() : 0;
			int expectedLength = expected != null ? expected.size() : 0;
			if (originalLength > 0 && originalLength == expectedLength) {
				if (request.getRound() == requestRound.currentRound(name)) {
					proposal = court.getProposal(name);
					if (proposal != null) {
						long newRound = requestRound.nextRound(name);
						request.setRound(newRound);
						if (request.getRound() == requestRound.currentRound(name)) {
							multicastGroup.multicast(ConsistencyRequest.LEARNING_OPERATION_REQUEST, request);
						}
					}
				}
			} else {
				if (original != null) {
					original.clear();
				}
				if (expected != null) {
					expected.clear();
				}
				if (request.hasExpired()) {
					multicastGroup.multicast(ConsistencyRequest.TIMEOUT_OPERATION_REQUEST, request);
				} else {
					if (request.getRound() == requestRound.currentRound(name)) {
						propose(name, proposal.getValue(), request.getTimeout());
					}
				}
			}
		}

	}

	/**
	 * 
	 * ConsistencyRequestPreparationFuture
	 *
	 * @author Jimmy Hoff
	 * @since 1.0
	 */
	private class ConsistencyRequestPreparationFuture extends ClockTask implements Formulation {

		private final ConsistencyRequest request;

		ConsistencyRequestPreparationFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		public void directRun() {
			runTask();
		}

		@Override
		public String getPeriod() {
			return PREPARATION_PERIOD;
		}

		@Override
		public boolean cancel() {
			return super.cancel();
		}

		@Override
		protected void runTask() {
			final String name = request.getName();
			final Proposal proposal = court.getProposal(name);
			List<ConsistencyResponse> responses = court.getProposal(name) != null ? court.getProposal(name).getPreparations() : null;
			int n = multicastGroup.countOfCandidate();
			if (responses != null && responses.size() > n / 2) {
				if (request.getRound() == requestRound.currentRound(name)) {
					Collections.sort(responses);
					final ConsistencyRequest firstRequest = responses.get(0).getRequest();

					for (ConsistencyResponse response : responses) {
						ConsistencyRequest request = response.getRequest();
						request.setValue(firstRequest.getValue() != null ? firstRequest.getValue() : proposal.getValue());
						multicastGroup.send(response.getApplicationInfo().getId(), ConsistencyRequest.COMMITMENT_OPERATION_REQUEST,
								request);
					}

					ConsistencyRequestCommitmentFuture commitmentFuture = new ConsistencyRequestCommitmentFuture(firstRequest);
					clock.schedule(commitmentFuture, responseWaitingTime, TimeUnit.SECONDS);
					court.formulate(name, commitmentFuture);
				}
			} else {
				if (responses != null) {
					responses.clear();
				}
				if (request.hasExpired()) {
					multicastGroup.multicast(ConsistencyRequest.TIMEOUT_OPERATION_REQUEST, request);
				} else {
					if (request.getRound() == requestRound.currentRound(name)) {
						propose(name, request.getValue(), request.getTimeout());
					}
				}
			}
		}
	}

}
