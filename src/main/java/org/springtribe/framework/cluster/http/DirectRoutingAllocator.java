package org.springtribe.framework.cluster.http;

/**
 * 
 * DirectRoutingAllocator
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class DirectRoutingAllocator implements RoutingAllocator {

	@Override
	public String allocateHost(String provider, String path) {
		return provider + path;
	}

}
