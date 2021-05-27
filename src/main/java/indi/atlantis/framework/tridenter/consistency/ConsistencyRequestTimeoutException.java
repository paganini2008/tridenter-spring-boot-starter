package indi.atlantis.framework.tridenter.consistency;

/**
 * 
 * ConsistencyRequestTimeoutException
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsistencyRequestTimeoutException extends ConsistencyRequestException {

	private static final long serialVersionUID = -8547355390875394014L;

	public ConsistencyRequestTimeoutException(String name, long serial, long round, int timeout) {
		super(name, serial, round);
		this.timeout = timeout;
	}

	private final int timeout;

	public int getTimeout() {
		return timeout;
	}

}
