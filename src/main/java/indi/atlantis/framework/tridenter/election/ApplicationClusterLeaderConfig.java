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
package indi.atlantis.framework.tridenter.election;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import indi.atlantis.framework.tridenter.http.EnableRestClient;
import indi.atlantis.framework.tridenter.http.LeaderService;

/**
 * 
 * ApplicationClusterLeaderConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@EnableRestClient(include = { LeaderService.class })
@Configuration
public class ApplicationClusterLeaderConfig {

	@Bean
	public LeaderElectionListener leaderElectionListener() {
		return new ApplicationLeaderElectionListener();
	}

	@Bean
	public ApplicationLeaderRecoveryListener applicationLeaderRecoveryListener() {
		return new ApplicationLeaderRecoveryListener();
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

}
