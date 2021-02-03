package org.springtribe.framework.cluster.http;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * StatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface StatisticIndicator {

	Statistic compute(String provider, Request request);

	Collection<Statistic> toCollection(String provider);

	Map<String, Collection<Statistic>> toMap();

}