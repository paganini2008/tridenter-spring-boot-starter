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

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import com.github.paganini2008.devtools.CharsetUtils;

/**
 * 
 * DigestInstanceIdGenerator
 * 
 * @author Fred Feng
 * @since 2.0.1
 */
public class DigestInstanceIdGenerator implements InstanceIdGenerator {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public String generateInstanceId() {
		String identifier = clusterName + "@" + UUID.randomUUID().toString().replace("-", "");
		return DigestUtils.md5DigestAsHex(identifier.getBytes(CharsetUtils.UTF_8));
	}

}
