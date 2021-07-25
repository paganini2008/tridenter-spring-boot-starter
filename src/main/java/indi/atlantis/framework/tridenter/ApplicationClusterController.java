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
package indi.atlantis.framework.tridenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * ApplicationClusterController
 * 
 * @author Fred Feng
 * @version 1.0
 */
@RequestMapping("/application/cluster")
@RestController
public class ApplicationClusterController {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ApplicationClusterContext leaderContext;

	@GetMapping("/ping")
	public ResponseEntity<ApplicationInfo> ping() {
		return ResponseEntity.ok(instanceId.getApplicationInfo());
	}

	@GetMapping("/state")
	public ResponseEntity<LeaderState> state() {
		return ResponseEntity.ok(leaderContext.getLeaderState());
	}

	@GetMapping("/list")
	public ResponseEntity<ApplicationInfo[]> list() {
		ApplicationInfo[] infos = applicationMulticastGroup.getCandidates();
		return ResponseEntity.ok(infos);
	}

}
