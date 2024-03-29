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
package io.atlantisframework.tridenter.multicast;

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
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@RequestMapping("/application/cluster")
@RestController
public class ApplicationMulticastController {

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Value("${spring.application.name}")
	private String applicationName;

	@GetMapping("/multicast")
	public ResponseEntity<String> multicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		applicationMulticastGroup.multicast(topic, content);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/unicast")
	public ResponseEntity<String> unicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		applicationMulticastGroup.unicast(topic, content);
		return ResponseEntity.ok("ok");
	}

}
