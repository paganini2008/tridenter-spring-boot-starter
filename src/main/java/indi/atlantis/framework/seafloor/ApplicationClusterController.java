package indi.atlantis.framework.seafloor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import indi.atlantis.framework.seafloor.multicast.ApplicationMulticastGroup;

/**
 * 
 * ApplicationClusterController
 * 
 * @author Jimmy Hoff
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
	public ResponseEntity<HealthState> state() {
		return ResponseEntity.ok(leaderContext.getHealthState());
	}

	@GetMapping("/list")
	public ResponseEntity<ApplicationInfo[]> list() {
		ApplicationInfo[] infos = applicationMulticastGroup.getCandidates();
		return ResponseEntity.ok(infos);
	}

}
