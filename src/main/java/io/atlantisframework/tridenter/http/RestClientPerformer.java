/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.http;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 
 * RestClientPerformer
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public interface RestClientPerformer {

	<T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Object... uriVariables);

	<T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Map<String, Object> uriParameters);

	<T> T perform(String url, HttpMethod method, Object requestBody, RestTemplateCallback<T> callback, Object... uriVariables);

	<T> T perform(String url, HttpMethod method, Object requestBody, RestTemplateCallback<T> callback, Map<String, Object> uriParameters);
}