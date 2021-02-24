package indi.atlantis.framework.seafloor.http;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * DirectRoutingAllocator
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class DirectRoutingAllocator implements RoutingAllocator {

	private final boolean testUrl;

	public DirectRoutingAllocator() {
		this(false);
	}

	public DirectRoutingAllocator(boolean testUrl) {
		this.testUrl = testUrl;
	}

	@Override
	public String allocateHost(String provider, String path, Request request) {
		String url = provider + path;
		if (testUrl) {
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				throw new RoutingPolicyException("Invalid url: " + url, e);
			}
		}
		return url;
	}

}
