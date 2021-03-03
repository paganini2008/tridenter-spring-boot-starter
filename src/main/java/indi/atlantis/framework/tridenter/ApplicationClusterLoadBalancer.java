package indi.atlantis.framework.tridenter;

import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.collection.CollectionUtils;

import indi.atlantis.framework.reditools.common.RedisAtomicLongSequence;

/**
 * 
 * ApplicationClusterLoadBalancer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationClusterLoadBalancer implements LoadBalancer {

	private final RedisAtomicLongSequence counter;

	public ApplicationClusterLoadBalancer(String name, RedisConnectionFactory connectionFactory) {
		this.counter = new RedisAtomicLongSequence(name, connectionFactory);
	}

	@Override
	public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
		if (CollectionUtils.isEmpty(candidates)) {
			return null;
		}
		return candidates.get((int) (counter.getAndIncrement() % candidates.size()));
	}

}
