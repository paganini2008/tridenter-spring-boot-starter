package com.github.dingo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import com.github.doodler.common.timeseries.Metric;



/**
 * 
 * @Description: NioSample
 * @Author: Fred Feng
 * @Date: 21/01/2025
 * @Version 1.0.0
 */
public class NioSample implements Metric {

    public final LongAdder totalExecutions = new LongAdder();
    public final LongAdder accumulatedExecutionTime = new LongAdder();
    private final List<Integer> rates = new ArrayList<>();

    public long getTotalExecutionCount() {
        return totalExecutions.longValue();
    }

    public long getAverageResponseTime() {
        long total = getTotalExecutionCount();
        if (total == 0) {
            return 0L;
        }
        return Math.round((double) accumulatedExecutionTime.longValue() / total);
    }

    public int getRate() {
        return (int) Math.round(rates.stream().mapToInt(Integer::intValue).average().getAsDouble());
    }

    @Override
    public void setRate(int rate) {
        this.rates.add(rate);
    }

    @Override
    public Map<String, Object> represent() {
        return Map.of("totalExecutionCount", getTotalExecutionCount(), "averageResponseTime",
                getAverageResponseTime(), "rate", getRate());
    }

}
