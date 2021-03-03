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
 * @author Jimmy Hoff
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
