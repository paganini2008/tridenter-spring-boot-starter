/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
