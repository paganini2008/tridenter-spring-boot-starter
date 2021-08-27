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
package indi.atlantis.framework.tridenter.ccr;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;

import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * CcrPlatform
 *
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class CcrPlatform {

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	private final Observable watcher = Observable.unrepeatable();
	private final Map<String, Proposal> juege = new ConcurrentHashMap<String, Proposal>();

	public boolean hasProposal(String name) {
		return juege.containsKey(name);
	}

	public boolean saveProposal(Proposal proposal) {
		if (hasProposal(proposal.getName())) {
			return false;
		}
		return juege.putIfAbsent(proposal.getName(), proposal) == null;
	}

	public Proposal getProposal(String name) {
		return juege.get(name);
	}

	public Proposal completeProposal(String name) {
		watcher.deleteObservers(Formulation.PREPARATION_PERIOD + name);
		watcher.deleteObservers(Formulation.COMMITMENT_PERIOD + name);
		return juege.remove(name);
	}

	public void formulate(String name, Formulation formulation) {
		watcher.addObserver(formulation.getPeriod() + name, (ob, arg) -> {
			formulation.cancel();
			formulation.directRun();
		});
	}

	public void canLearn(CcrResponse response) {
		CcrRequest request = response.getRequest();
		String name = request.getName();
		if (juege.containsKey(name)) {
			List<CcrResponse> list = juege.get(name).getCommitments();
			list.add(response);
			if (list.size() == applicationMulticastGroup.countOfCandidate()) {
				watcher.notifyObservers(Formulation.COMMITMENT_PERIOD + name, name);
			}
		}
	}

	public void canCommit(CcrResponse response) {
		CcrRequest request = response.getRequest();
		String name = request.getName();
		if (juege.containsKey(name)) {
			List<CcrResponse> list = juege.get(name).getPreparations();
			list.add(response);
			if (list.size() == applicationMulticastGroup.countOfCandidate()) {
				watcher.notifyObservers(Formulation.PREPARATION_PERIOD + name, name);
			}
		}
	}

	public static class Proposal {

		private final String name;
		private final Object value;
		private final List<CcrResponse> preparations = new CopyOnWriteArrayList<CcrResponse>();
		private final List<CcrResponse> commitments = new CopyOnWriteArrayList<CcrResponse>();

		Proposal(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public List<CcrResponse> getPreparations() {
			return preparations;
		}

		public List<CcrResponse> getCommitments() {
			return commitments;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

	}

}
