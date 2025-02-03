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

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.doodler.common.ApiResult;

/**
 * 
 * @Description: PerformanceInspectorController
 * @Author: Fred Feng
 * @Date: 21/01/2025
 * @Version 1.0.0
 */
@RequestMapping("/sys/transmitter")
@RestController
public class PerformanceInspectorController {

    @Autowired
    private PerformanceInspectorService performanceInspectorService;

    @GetMapping("/instances")
    public ApiResult<Collection<String>> categories() {
        return ApiResult.ok(performanceInspectorService.categories());
    }

    @GetMapping("/sequence")
    public ApiResult<Map<String, Object>> sequence(@RequestParam("instanceId") String instanceId,
            @RequestParam("mode") String mode) {
        return ApiResult.ok(performanceInspectorService.sequence(instanceId, mode,
                DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @GetMapping("/summarize")
    public ApiResult<Object> summarize(@RequestParam("instanceId") String instanceId,
            @RequestParam("mode") String mode) {
        return ApiResult.ok(
                performanceInspectorService.summarize(instanceId, mode).getSample().represent());
    }

}
