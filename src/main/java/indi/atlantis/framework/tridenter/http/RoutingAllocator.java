package indi.atlantis.framework.tridenter.http;

/**
 * 
 * RoutingAllocator
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RoutingAllocator {

	static final String ALL = "*";

	static final String LEADER = "L";

	String allocateHost(String provider, String path, Request request);

}
