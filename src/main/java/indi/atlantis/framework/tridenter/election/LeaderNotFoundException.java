package indi.atlantis.framework.tridenter.election;

/**
 * 
 * LeaderNotFoundException
 *
 * @author Fred Feng
 * @since 1.0
 */
public class LeaderNotFoundException extends IllegalStateException {

	private static final long serialVersionUID = -1790240240012477711L;

	public LeaderNotFoundException() {
		super();
	}

	public LeaderNotFoundException(String msg) {
		super(msg);
	}

}
