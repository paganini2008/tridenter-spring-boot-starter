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
 * SpringMvcRequests
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public abstract class SpringMvcRequests {

	public static class GenericRequest extends ParameterizedRequestImpl {

		GenericRequest(RequestMapping mapping) {
			super(mapping.value()[0], HttpMethod.valueOf(mapping.method()[0].name()));
			applyDefaultSettings(this, mapping.consumes(), mapping.produces());
		}

	}

	public static class GetRequest extends ParameterizedRequestImpl {

		GetRequest(GetMapping mapping) {
			super(mapping.value()[0], HttpMethod.GET);
			applyDefaultSettings(this, mapping.consumes(), mapping.produces());
		}

	}

	public static class PostRequest extends ParameterizedRequestImpl {

		PostRequest(PostMapping mapping) {
			super(mapping.value()[0], HttpMethod.POST);
			applyDefaultSettings(this, mapping.consumes(), mapping.produces());
		}

	}

	public static class PutRequest extends ParameterizedRequestImpl {

		PutRequest(PutMapping mapping) {
			super(mapping.value()[0], HttpMethod.PUT);
			applyDefaultSettings(this, mapping.consumes(), mapping.produces());
		}

	}

	public static class DeleteRequest extends ParameterizedRequestImpl {

		DeleteRequest(DeleteMapping mapping) {
			super(mapping.value()[0], HttpMethod.DELETE);
			applyDefaultSettings(this, mapping.consumes(), mapping.produces());
		}

	}

	private static void applyDefaultSettings(ParameterizedRequest request, String[] consumes, String[] produces) {
		if (ArrayUtils.isNotEmpty(consumes)) {
			request.getHeaders().setContentType(MediaType.parseMediaType(consumes[0]));
		}
		if (ArrayUtils.isNotEmpty(produces)) {
			List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
			for (String produce : produces) {
				acceptableMediaTypes.add(MediaType.parseMediaType(produce));
			}
			request.getHeaders().setAccept(acceptableMediaTypes);
		}
	}

}
