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
package indi.atlantis.framework.tridenter.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * EmbeddedServerProperties
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@ConfigurationProperties(prefix = "spring.application.cluster.gateway")
@Getter
@Setter
public class EmbeddedServerProperties {

	private int port = 7000;
	private String hostName;
	private Netty netty = new Netty();

	@Getter
	@Setter
	public static class Netty {

		private int bossGroupThreads = 16;
		private int workGroupThreads = 16;
		private int idleTimeout = 60;
		private int maxContentLength = 65536;
		private int maxInitialLineLength = 4096;
		private int maxHeaderSize = 8192;
		private int maxChunkSize = 8192;
		private boolean corsEnabled = true;
		private long maxAge = 0;
		private boolean gzipEnabled = false;
	}

}
