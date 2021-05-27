package indi.atlantis.framework.tridenter;

import java.util.List;

/**
 * 
 * LoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LoadBalancer {

	ApplicationInfo select(Object message, List<ApplicationInfo> candidates);

}
