package org.springtribe.framework.cluster.consistency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * Court
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class Court {

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

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

	public void canLearn(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		String name = request.getName();
		if (juege.containsKey(name)) {
			List<ConsistencyResponse> list = juege.get(name).getCommitments();
			list.add(response);
			if (list.size() == multicastGroup.countOfCandidate()) {
				watcher.notifyObservers(Formulation.COMMITMENT_PERIOD + name, name);
			}
		}
	}

	public void canCommit(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		String name = request.getName();
		if (juege.containsKey(name)) {
			List<ConsistencyResponse> list = juege.get(name).getPreparations();
			list.add(response);
			if (list.size() == multicastGroup.countOfCandidate()) {
				watcher.notifyObservers(Formulation.PREPARATION_PERIOD + name, name);
			}
		}
	}

	public static class Proposal {

		private final String name;
		private final Object value;
		private final List<ConsistencyResponse> preparations = new CopyOnWriteArrayList<ConsistencyResponse>();
		private final List<ConsistencyResponse> commitments = new CopyOnWriteArrayList<ConsistencyResponse>();

		Proposal(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public List<ConsistencyResponse> getPreparations() {
			return preparations;
		}

		public List<ConsistencyResponse> getCommitments() {
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
