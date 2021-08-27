/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package indi.atlantis.framework.tridenter.http;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LeaderState;

/**
 * 
 * LeaderService
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@RestClient(provider = RoutingAllocator.LEADER)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET, retries = 3, timeout = 60)
	ApplicationInfo ping();

	@Api(path = "/application/cluster/state", method = HttpMethod.GET, retries = 3, timeout = 60)
	LeaderState state();

	@Api(path = "/application/cluster/list", method = HttpMethod.GET)
	ApplicationInfo[] list();

	@Api(path = "/application/cluster/online/{appId}", method = HttpMethod.POST)
	String online(@PathVariable("appId") String appId);

	@Api(path = "/application/cluster/offline/{appId}", method = HttpMethod.POST)
	String offline(@PathVariable("appId") String appId);

	@Api(path = "/application/cluster/recovery", method = HttpMethod.GET)
	ApplicationInfo[] recovery();

}
