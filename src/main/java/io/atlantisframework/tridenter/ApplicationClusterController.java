/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.StringUtils;

import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * ApplicationClusterController
 * 
 * @author Fred Feng
 * @since 2.0.1
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
	private ApplicationClusterContext applicationClusterContext;

	@GetMapping("/ping")
	public ResponseEntity<ApplicationInfo> ping() {
		return ResponseEntity.ok(instanceId.getApplicationInfo());
	}

	@GetMapping("/state")
	public ResponseEntity<LeaderState> state() {
		return ResponseEntity.ok(applicationClusterContext.getLeaderState());
	}

	@GetMapping("/list")
	public ResponseEntity<ApplicationInfo[]> list(@RequestParam(name = "group", required = false) String group) {
		ApplicationInfo[] infos = StringUtils.isNotBlank(group) ? applicationMulticastGroup.getCandidates(group)
				: applicationMulticastGroup.getCandidates();
		return ResponseEntity.ok(infos);
	}

	@GetMapping("/offline/list")
	public ResponseEntity<ApplicationInfo[]> offlineList() {
		ApplicationInfo[] infos = applicationMulticastGroup.getOfflineCandidates();
		return ResponseEntity.ok(infos);
	}

	@PostMapping("/offline/{appId}")
	public ResponseEntity<String> offline(@PathVariable("appId") String appId) {
		applicationMulticastGroup.offline(appId);
		return ResponseEntity.ok(appId);
	}

	@PostMapping("/online/{appId}")
	public ResponseEntity<String> online(@PathVariable("appId") String appId) {
		applicationMulticastGroup.online(appId);
		return ResponseEntity.ok(appId);
	}

}
