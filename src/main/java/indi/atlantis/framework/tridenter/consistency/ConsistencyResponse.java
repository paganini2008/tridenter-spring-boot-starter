package indi.atlantis.framework.tridenter.consistency;

import java.io.Serializable;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * ConsistencyResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@ToString
@Setter
@Getter
public class ConsistencyResponse implements Serializable, Comparable<ConsistencyResponse> {
	
	private static final long serialVersionUID = 704634098311834803L;

	public ConsistencyResponse() {
	}

	public ConsistencyResponse(ConsistencyRequest request, ApplicationInfo applicationInfo, boolean acceptable) {
		this.request = request;
		this.applicationInfo = applicationInfo;
		this.acceptable = acceptable;
	}

	private ConsistencyRequest request;
	private ApplicationInfo applicationInfo;
	private boolean acceptable;

	@Override
	public int compareTo(ConsistencyResponse other) {
		long otherSerial = other.getRequest().getSerial();
		if (otherSerial == request.getSerial()) {
			return other.getApplicationInfo().getId().compareTo(applicationInfo.getId());
		}
		return (int) (otherSerial - request.getSerial());
	}

}
