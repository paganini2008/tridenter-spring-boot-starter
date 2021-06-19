/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.http;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.paganini2008.devtools.ArrayUtils;

/**
 * 
 * RequestBuilder
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class RequestBuilder {

	public static class GetRequest extends ParameterizedRequestImpl {

		GetRequest(GetMapping mapping) {
			super(mapping.value()[0], HttpMethod.GET);
			applySettings(mapping.annotationType().getAnnotation(RequestMapping.class), this);
		}

	}

	public static class PostRequest extends ParameterizedRequestImpl {

		PostRequest(PostMapping mapping) {
			super(mapping.value()[0], HttpMethod.POST);
			applySettings(mapping.annotationType().getAnnotation(RequestMapping.class), this);
		}

	}

	public static class PutRequest extends ParameterizedRequestImpl {

		PutRequest(PutMapping mapping) {
			super(mapping.value()[0], HttpMethod.PUT);
			applySettings(mapping.annotationType().getAnnotation(RequestMapping.class), this);
		}

	}

	public static class DeleteRequest extends ParameterizedRequestImpl {

		DeleteRequest(DeleteMapping mapping) {
			super(mapping.value()[0], HttpMethod.DELETE);
			applySettings(mapping.annotationType().getAnnotation(RequestMapping.class), this);
		}

	}

	private static void applySettings(RequestMapping requestMapping, BasicRequest basicRequest) {
		if (ArrayUtils.isNotEmpty(requestMapping.produces())) {
			List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
			for (String produce : requestMapping.produces()) {
				acceptableMediaTypes.add(MediaType.parseMediaType(produce));
			}
			basicRequest.getHeaders().setAccept(acceptableMediaTypes);
		}
	}

}
