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
package io.atlantisframework.tridenter.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * ApplicationContextController
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@RequestMapping("/application/context")
@RestController
public class ApplicationContextController {

	@GetMapping("/bean/count")
	public ResponseEntity<Integer> getBeanCount() {
		int beanCount = ApplicationContextUtils.countOfBeans();
		return ResponseEntity.ok(beanCount);
	}

	@GetMapping("/bean/names")
	public ResponseEntity<String[]> getBeanNames() {
		String[] beanNames = ApplicationContextUtils.getAllBeanNames();
		return ResponseEntity.ok(beanNames);
	}

	@GetMapping("/bean/list")
	public ResponseEntity<List<SimpleBeanInfo>> getBeanInfos(@RequestParam(name = "className", required = false) String className) {
		Map<String, ?> beanMap = StringUtils.isNotBlank(className) ? ApplicationContextUtils.getAllBeans()
				: ApplicationContextUtils.getBeansOfType(ClassUtils.forName(className));
		List<SimpleBeanInfo> results = beanMap.entrySet().stream()
				.map(e -> new SimpleBeanInfo(e.getKey(), e.getValue().getClass().getName())).collect(Collectors.toList());
		return ResponseEntity.ok(results);
	}

}
