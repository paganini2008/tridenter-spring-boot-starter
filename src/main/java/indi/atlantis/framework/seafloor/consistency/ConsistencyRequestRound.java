package indi.atlantis.framework.seafloor.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springtribe.framework.reditools.common.TtlKeeper;

import com.github.paganini2008.devtools.collection.MapUtils;

import indi.atlantis.framework.seafloor.Constants;

/**
 * 
 * ConsistencyRequestRound
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class ConsistencyRequestRound {

	private static final String CONSISTENCY_ROUND_PATTERN = "%s:consistency:round:%s";
	private final Map<String, RedisAtomicLong> rounds = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	@Autowired
	private TtlKeeper ttlKeeper;

	public long nextRound(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(rounds, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			}).incrementAndGet();
		} catch (Exception e) {
			rounds.remove(name);
			return nextRound(name);
		}
	}

	public long currentRound(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(rounds, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			}).get();
		} catch (Exception e) {
			rounds.remove(name);
			return currentRound(name);
		}
	}

	public void clean(String name) {
		rounds.remove(name);
	}

	private String counterName(String name) {
		return String.format(CONSISTENCY_ROUND_PATTERN, Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName, name);
	}

}
