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
package indi.atlantis.framework.tridenter.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

import indi.atlantis.framework.tridenter.http.FallbackProvider;
import indi.atlantis.framework.tridenter.http.ForwardedRequest;
import indi.atlantis.framework.tridenter.http.Request;
import indi.atlantis.framework.tridenter.http.RequestTemplate;
import indi.atlantis.framework.tridenter.utils.ApplicationContextUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DynamicResourceResolver
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class DynamicResourceResolver implements ResourceResolver {

	@Autowired
	private RequestTemplate requestTemplate;

	@Qualifier("dynamicResourceCache")
	@Autowired
	private Cache cache;

	@Override
	public void resolve(FullHttpRequest httpRequest, Router router, String path, ChannelHandlerContext ctx) throws Exception {
		Request request = makeRequest(httpRequest, router, path);
		String body;
		HttpStatus httpStatus;
		HttpHeaders httpHeaders;
		if (router.cached()) {
			CachedResponseEntity cachedObject = (CachedResponseEntity) cache.getObject(path);
			if (cachedObject == null) {
				ResponseEntity<String> responseEntity = requestTemplate.sendRequest(router.provider(), request, String.class);
				cache.putObject(path,
						new CachedResponseEntity(responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCode()));
				cachedObject = (CachedResponseEntity) cache.getObject(path);
			}
			body = (String) cachedObject.getBody();
			httpStatus = cachedObject.getStatus();
			httpHeaders = cachedObject.getHeaders();
		} else {
			ResponseEntity<String> responseEntity = requestTemplate.sendRequest(router.provider(), request, String.class);
			body = responseEntity.getBody();
			httpStatus = responseEntity.getStatusCode();
			httpHeaders = responseEntity.getHeaders();
		}
		ByteBuf buffer = Unpooled.copiedBuffer(body, router.charset());
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(httpRequest.protocolVersion(),
				HttpResponseStatus.valueOf(httpStatus.value()), buffer);
		httpResponse.headers().set(CONTENT_LENGTH, buffer.readableBytes());
		MediaType mediaType = httpHeaders.getContentType();
		if (mediaType == null) {
			mediaType = MediaType.APPLICATION_JSON;
		}
		httpResponse.headers().set(CONTENT_TYPE, mediaType.toString());
		boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
		if (keepAlive) {
			httpResponse.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		ChannelFuture future = ctx.writeAndFlush(httpResponse);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	private Request makeRequest(FullHttpRequest httpRequest, Router router, String rawPath) {
		HttpHeaders httpHeaders = copyHttpHeaders(httpRequest);
		if (MapUtils.isNotEmpty(router.defaultHeaders())) {
			httpHeaders.addAll(router.defaultHeaders());
		}
		if (CollectionUtils.isNotEmpty(router.ignoredHeaders())) {
			MapUtils.removeKeys(httpHeaders, router.ignoredHeaders());
		}
		ByteBuf byteBuf = httpRequest.content();
		byte[] body = null;
		int length = byteBuf.readableBytes();
		if (length > 0) {
			body = new byte[length];
			byteBuf.readBytes(body);
		}
		ForwardedRequest request = new ForwardedRequest(rawPath, HttpMethod.valueOf(httpRequest.method().name()), httpHeaders);
		request.setBody(body);
		request.setTimeout(router.timeout());
		request.setRetries(router.retries());
		request.setAllowedPermits(router.allowedPermits());
		request.setFallback(getFallback(router.fallback()));
		return request;
	}

	private FallbackProvider getFallback(Class<?> fallbackClass) {
		try {
			if (fallbackClass != null && fallbackClass != Void.class && fallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getOrCreateBean(fallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	protected HttpHeaders copyHttpHeaders(FullHttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

}
