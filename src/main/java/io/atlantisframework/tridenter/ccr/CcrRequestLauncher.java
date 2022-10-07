/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter.ccr;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.ClockTask;

import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.ccr.CcrPlatform.Proposal;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrRequestLauncher
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public final class CcrRequestLauncher {

	public static final long CCR_REQUEST_MAX_TIMEOUT = 60000;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private Clock clock;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Qualifier("batchNoGenerator")
	@Autowired
	private CcrSerialNoGenerator batchNoGenerator;

	@Qualifier("serialNoGenerator")
	@Autowired
	private CcrSerialNoGenerator serialNoGenerator;

	@Value("${atlantis.framework.tridenter.election.ccr.responseWaitingTime:1}")
	private long responseWaitingTime;

	@Autowired
	private CcrPlatform ccrPlatform;

	public boolean propose(String name, Object value, long timeout) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Proposal name is a must.");
		}
		if (timeout > CCR_REQUEST_MAX_TIMEOUT) {
			throw new IllegalArgumentException("Maximum timout is " + CCR_REQUEST_MAX_TIMEOUT);
		}
		if (ccrPlatform.hasProposal(name)) {
			if (log.isTraceEnabled()) {
				log.trace("The proposal named '{}' is being processing currently. Please submit again after completion.", name);
			}
			return false;
		}
		Proposal proposal = new Proposal(name, value);
		if (!ccrPlatform.saveProposal(proposal)) {
			if (log.isTraceEnabled()) {
				log.trace("The proposal named '{}' is being processing currently. Please submit again after completion.", name);
			}
			return false;
		}
		final long batchNo = batchNoGenerator.currentSerialNo(name);
		final long serialNo = serialNoGenerator.nextSerialNo(name) + (instanceId.getWeight() > 1 ? instanceId.getWeight() : 0);
		CcrRequest request = CcrRequest.of(instanceId.getApplicationInfo()).setName(name).setBatchNo(batchNo).setSerialNo(serialNo)
				.setTimeout(timeout);
		applicationMulticastGroup.multicast(CcrRequest.PREPARATION_REQUEST, request);

		CcrRequestPreparationFuture preparationFuture = new CcrRequestPreparationFuture(request);
		clock.schedule(preparationFuture, responseWaitingTime, TimeUnit.SECONDS);
		ccrPlatform.formulate(name, preparationFuture);
		return true;
	}

	public void sync(String anotherInstanceId, String name, Object value) {
		final long batchNo = batchNoGenerator.currentSerialNo(name);
		final long serialNo = serialNoGenerator.nextSerialNo(name);
		CcrRequest request = CcrRequest.of(instanceId.getApplicationInfo()).setName(name).setValue(value).setBatchNo(batchNo)
				.setSerialNo(serialNo).setTimeout(0);
		applicationMulticastGroup.send(anotherInstanceId, CcrRequest.LEARNING_REQUEST, request);
	}

	/**
	 * 
	 * CcrRequestCommitmentFuture
	 *
	 * @author Fred Feng
	 * @since 2.0.1
	 */
	private class CcrRequestCommitmentFuture extends ClockTask implements Formulation {

		private final CcrRequest request;

		CcrRequestCommitmentFuture(CcrRequest request) {
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
			final Proposal proposal = ccrPlatform.getProposal(name);
			List<CcrResponse> original = proposal != null ? proposal.getCommitments() : null;
			List<CcrResponse> expected = proposal != null ? proposal.getPreparations() : null;
			int originalLength = original != null ? original.size() : 0;
			int expectedLength = expected != null ? expected.size() : 0;
			if (originalLength > 0 && originalLength == expectedLength) {
				if (request.getBatchNo() == batchNoGenerator.currentSerialNo(name)) {
					long newBatchNo = batchNoGenerator.nextSerialNo(name);
					request.setBatchNo(newBatchNo);
					request.setValue(proposal.getValue());
					if (request.getBatchNo() == batchNoGenerator.currentSerialNo(name)) {
						applicationMulticastGroup.multicast(CcrRequest.LEARNING_REQUEST, request);
					}
				}
			} else {
				if (original != null) {
					original.clear();
				}
				if (expected != null) {
					expected.clear();
				}
				if (request.hasExpired(System.currentTimeMillis())) {
					applicationMulticastGroup.multicast(CcrRequest.TIMEOUT_REQUEST, request);
				} else {
					if (request.getBatchNo() == batchNoGenerator.currentSerialNo(name)) {
						propose(name, proposal.getValue(), request.getTimeout());
					}
				}
			}
		}

	}

	/**
	 * 
	 * CcrRequestPreparationFuture
	 *
	 * @author Fred Feng
	 * @since 2.0.1
	 */
	private class CcrRequestPreparationFuture extends ClockTask implements Formulation {

		private final CcrRequest request;

		CcrRequestPreparationFuture(CcrRequest request) {
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
			final Proposal proposal = ccrPlatform.getProposal(name);
			List<CcrResponse> responses = ccrPlatform.getProposal(name) != null ? ccrPlatform.getProposal(name).getPreparations() : null;
			int nCandidates = applicationMulticastGroup.countOfCandidate();
			if (responses != null && responses.size() > nCandidates / 2) {
				if (request.getBatchNo() == batchNoGenerator.currentSerialNo(name)) {
					for (CcrResponse response : responses) {
						CcrRequest request = response.getRequest();
						request.setValue(proposal.getValue());
						applicationMulticastGroup.send(response.getApplicationInfo().getId(), CcrRequest.COMMITMENT_REQUEST, request);
					}

					CcrRequestCommitmentFuture commitmentFuture = new CcrRequestCommitmentFuture(request);
					clock.schedule(commitmentFuture, responseWaitingTime, TimeUnit.SECONDS);
					ccrPlatform.formulate(name, commitmentFuture);
				}
			} else {
				if (responses != null) {
					responses.clear();
				}
				if (request.hasExpired(System.currentTimeMillis())) {
					applicationMulticastGroup.multicast(CcrRequest.TIMEOUT_REQUEST, request);
				} else {
					if (request.getBatchNo() == batchNoGenerator.currentSerialNo(name)) {
						propose(name, proposal.getValue(), request.getTimeout());
					}
				}
			}
		}
	}

}
