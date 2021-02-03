package org.springtribe.framework.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * ApplicationMulticastController
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@RequestMapping("/application/cluster")
@RestController
public class ApplicationMulticastController {

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Value("${spring.application.name}")
	private String applicationName;

	@GetMapping("/multicast")
	public ResponseEntity<String> multicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		multicastGroup.multicast(topic, content);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/unicast")
	public ResponseEntity<String> unicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		multicastGroup.unicast(topic, content);
		return ResponseEntity.ok("ok");
	}

}
