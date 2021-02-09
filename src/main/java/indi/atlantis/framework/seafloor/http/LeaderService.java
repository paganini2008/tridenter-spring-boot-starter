package indi.atlantis.framework.seafloor.http;

import org.springframework.http.HttpMethod;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import indi.atlantis.framework.seafloor.LeaderState;

/**
 * 
 * LeaderService
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@RestClient(provider = RoutingAllocator.LEADER)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET, retries = 3, timeout = 60)
	ApplicationInfo ping();

	@Api(path = "/application/cluster/state", method = HttpMethod.GET, retries = 3, timeout = 60)
	LeaderState state();

	@Api(path = "/application/cluster/list", method = HttpMethod.GET)
	ApplicationInfo[] list();

	@Api(path = "/application/cluster/recovery", method = HttpMethod.GET)
	ApplicationInfo[] recovery();

}
