package indi.atlantis.framework.tridenter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import com.github.paganini2008.devtools.CharsetUtils;

/**
 * 
 * Md5InstanceIdGenerator
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class Md5InstanceIdGenerator implements InstanceIdGenerator {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public String generateInstanceId() {
		String identifier = clusterName + "@" + UUID.randomUUID().toString().replace("-", "");
		return DigestUtils.md5DigestAsHex(identifier.getBytes(CharsetUtils.UTF_8));
	}

}
