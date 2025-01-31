package com.github.dingo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.github.doodler.common.context.ManagedBeanLifeCycle;
import com.github.doodler.common.timeseries.LoggingOverflowDataHandler;
import com.github.doodler.common.timeseries.OverflowDataHandler;
import com.github.doodler.common.timeseries.RateCalculator;
import com.github.doodler.common.timeseries.Sampler;
import com.github.doodler.common.timeseries.SamplerImpl;
import com.github.doodler.common.timeseries.StringSamplerService;
import com.github.doodler.common.utils.TimeWindowUnit;

/**
 * 
 * @Description: PerformanceInspectorService
 * @Author: Fred Feng
 * @Date: 21/01/2025
 * @Version 1.0.0
 */
public class PerformanceInspectorService extends StringSamplerService<NioSample>
        implements ManagedBeanLifeCycle {

    public PerformanceInspectorService() {
        this(Arrays.asList(new LoggingOverflowDataHandler<>()));
    }

    public PerformanceInspectorService(
            List<OverflowDataHandler<String, String, NioSample>> dataHandlers) {
        super(1, TimeWindowUnit.MINUTES, 60, dataHandlers);
    }

    private RateCalculator<String, String, NioSample, Sampler<NioSample>> rateCalculator;

    @Override
    protected Sampler<NioSample> getEmptySampler(String category, String dimension,
            long timestampMillis) {
        return new SamplerImpl<NioSample>(timestampMillis, new NioSample());
    }

    @Override
    public void update(String category, String dimension, long timestampMillis,
            Consumer<Sampler<NioSample>> consumer) {
        super.update(category, dimension, timestampMillis, consumer);
        rateCalculator.incr(category, dimension);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        rateCalculator = new RateCalculator<>(1, TimeUnit.SECONDS, this);
        rateCalculator.start();
    }

    @Override
    public void destroy() throws Exception {
        if (rateCalculator != null) {
            rateCalculator.stop();
        }
    }
}
