package indi.atlantis.framework.tridenter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * DefaultInstanceIdGenerator
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public class DefaultInstanceIdGenerator implements InstanceIdGenerator {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public String generateInstanceId() {
		return clusterName + "@" + UUID.randomUUID().toString().replace("-", "");
	}

}
