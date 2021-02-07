package indi.atlantis.framework.seafloor.consistency;

/**
 * 
 * ConsistencyRequestException
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ConsistencyRequestException extends IllegalStateException {

	private static final long serialVersionUID = -1376569536093800120L;

	public ConsistencyRequestException(String name, long serial, long round) {
		super(repr(name, serial, round));
		this.name = name;
		this.serial = serial;
		this.round = round;
	}

	public ConsistencyRequestException(String name, long serial, long round, Throwable e) {
		super(repr(name, serial, round), e);
		this.name = name;
		this.serial = serial;
		this.round = round;
	}

	private final String name;
	private final long serial;
	private final long round;

	public String getName() {
		return name;
	}

	public long getSerial() {
		return serial;
	}

	public long getRound() {
		return round;
	}

	private static String repr(String name, long serial, long round) {
		return name + "::" + serial + "::" + round;
	}

}
