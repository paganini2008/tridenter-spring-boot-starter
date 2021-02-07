package indi.atlantis.framework.seafloor.http;

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
