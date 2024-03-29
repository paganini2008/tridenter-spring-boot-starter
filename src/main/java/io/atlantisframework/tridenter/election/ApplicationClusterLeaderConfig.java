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
package io.atlantisframework.tridenter.election;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.atlantisframework.tridenter.http.EnableRestClient;
import io.atlantisframework.tridenter.http.LeaderService;

/**
 * 
 * ApplicationClusterLeaderConfig
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@EnableRestClient(include = { LeaderService.class })
@Configuration
public class ApplicationClusterLeaderConfig {

	@ConditionalOnMissingBean
	@Bean
	public LeaderElectionListener leaderElectionListener() {
		return new ApplicationLeaderElectionListener();
	}

	@ConditionalOnMissingBean
	@Bean
	public LeaderElection leaderElection() {
		return new ApplicationLeaderElection();
	}

	@ConditionalOnMissingBean
	@Bean
	public LeaderRecovery leaderRecovery() {
		return new DefaultLeaderRecovery();
	}
	
	@Bean
	public ApplicationLeaderRecoveryListener applicationLeaderRecoveryListener() {
		return new ApplicationLeaderRecoveryListener();
	}

}
