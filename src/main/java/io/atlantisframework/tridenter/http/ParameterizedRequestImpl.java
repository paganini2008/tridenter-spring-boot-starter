/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.ObjectUtils;

/**
 * 
 * ParameterizedRequestImpl
 *
 * @author Fred Feng
 * 
 * @since 2.0.1
 */
public class ParameterizedRequestImpl extends ForwardedRequest implements ParameterizedRequest {

	private Map<String, Object> requestParameters = new HashMap<String, Object>();
	private Map<String, Object> pathVariables = new HashMap<String, Object>();

	ParameterizedRequestImpl(String path, HttpMethod method) {
		this(path, method, new HttpHeaders());
	}

	ParameterizedRequestImpl(String path, HttpMethod method, HttpHeaders httpHeaders) {
		super(path, method, httpHeaders);
	}

	public Map<String, Object> getRequestParameters() {
		return requestParameters;
	}

	public Map<String, Object> getPathVariables() {
		return pathVariables;
	}

	public void accessParameter(Parameter parameter, @Nullable Object argument) {
		String parameterName;
		Annotation[] annotations = parameter.getAnnotations();
		if (ArrayUtils.isNotEmpty(annotations)) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == RequestHeader.class) {
					RequestHeader requestHeader = (RequestHeader) annotation;
					parameterName = ObjectUtils.toString(requestHeader.value(), parameter.getName());
					getHeaders().add(parameterName, argument != null ? (String) argument : requestHeader.defaultValue());
				}
				if (annotation.annotationType() == RequestParam.class) {
					RequestParam requestParam = (RequestParam) annotation;
					parameterName = ObjectUtils.toString(requestParam.value(), parameter.getName());
					requestParameters.put(parameterName, argument != null ? argument : requestParam.defaultValue());
				}
				if (annotation.annotationType() == PathVariable.class) {
					PathVariable pathVariable = (PathVariable) annotation;
					parameterName = ObjectUtils.toString(pathVariable.value(), parameter.getName());
					pathVariables.put(parameterName, argument);
				}
				if (annotation.annotationType() == RequestBody.class) {
					MediaType mediaType = getHeaders().getContentType();
					if (mediaType == null) {
						getHeaders().setContentType(MediaType.APPLICATION_JSON);
					}
					setBody(new HttpEntity<Object>(argument, getHeaders()));
				} else if (annotation.annotationType() == ModelAttribute.class) {
					MediaType mediaType = getHeaders().getContentType();
					if (mediaType == null) {
						getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
					}
					setBody(new HttpEntity<Object>(argument, getHeaders()));
				} else if (annotation.annotationType() == MultipartBody.class) {
					MediaType mediaType = getHeaders().getContentType();
					if (mediaType == null) {
						getHeaders().setContentType(MediaType.MULTIPART_FORM_DATA);
					}
					setBody(new HttpEntity<Object>(argument, getHeaders()));
				}
			}
		} else {
			requestParameters.put(parameter.getName(), argument);
		}

	}

}
