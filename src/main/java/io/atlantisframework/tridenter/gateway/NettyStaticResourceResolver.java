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
package io.atlantisframework.tridenter.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_DISPOSITION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.NumberUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.devtools.io.PathUtils;

import io.atlantisframework.tridenter.gateway.Router.ResourceType;
import io.atlantisframework.tridenter.http.ForwardedRequest;
import io.atlantisframework.tridenter.http.Request;
import io.atlantisframework.tridenter.http.RequestTemplate;
import io.atlantisframework.tridenter.http.RestTemplateDownloadHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyStaticResourceResolver
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class NettyStaticResourceResolver implements NettyHttpObjectResolver {

	@Autowired
	private RequestTemplate requestTemplate;

	@Qualifier("staticResourceCache")
	@Autowired
	private Cache cache;

	@Override
	public void resolve(HttpObject httpObject, Router router, String url, ChannelHandlerContext ctx) throws Exception {
		HttpRequest httpRequest = (HttpRequest) httpObject;
		if (log.isTraceEnabled()) {
			log.trace("Send request to path: {}", url);
		}
		switch (router.resourceType()) {
		case REDIRECT:
			NettyHttpUtils.sendRedirect(ctx, url);
			break;
		case STREAM:
		case FILE:
			applyResource(httpRequest, router, url, ctx);
			break;
		default:
			break;
		}

	}

	private void applyResource(HttpRequest httpRequest, Router router, String url, ChannelHandlerContext ctx) throws Exception {
		Request request = makeRequest(httpRequest, router, url);
		String fileName = PathUtils.getName(url.contains("?") ? url.substring(0, url.indexOf("?")) : url);
		if (StringUtils.isBlank(fileName)) {
			fileName = PathUtils.getName(router.prefix());
		}
		if (StringUtils.isBlank(fileName)) {
			fileName = UUID.randomUUID().toString();
		}
		File file;
		Map<String, String> responseHeaders = new LinkedHashMap<String, String>();
		if (router.cached()) {
			CachedResponseEntity cachedFile = (CachedResponseEntity) cache.getObject(url);
			if (cachedFile == null) {
				ResponseEntity<File> responseEntity = requestTemplate.sendRequest(router.provider(), request,
						new RestTemplateDownloadHandler(FileUtils.getTempDirectory(), fileName));
				cache.putObject(url,
						new CachedResponseEntity(responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCode()));
				cachedFile = (CachedResponseEntity) cache.getObject(url);
			}
			file = (File) cachedFile.getBody();
			responseHeaders.putAll(cachedFile.getHeaders().toSingleValueMap());
		} else {
			ResponseEntity<File> responseEntity = requestTemplate.sendRequest(router.provider(), request,
					new RestTemplateDownloadHandler(FileUtils.getTempDirectory(), fileName));
			file = responseEntity.getBody();
			responseHeaders.putAll(responseEntity.getHeaders().toSingleValueMap());
		}
		HttpResponse httpResponse = new DefaultHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
		for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
			httpResponse.headers().set(entry.getKey(), entry.getValue());
		}
		httpResponse.headers().set(CONTENT_LENGTH, file.length());
		String contentType = httpResponse.headers().get(CONTENT_TYPE);
		if (StringUtils.isBlank(contentType)) {
			try {
				contentType = Files.probeContentType(file.toPath());
			} catch (IOException ignored) {
			}
		}
		if (StringUtils.isBlank(contentType)) {
			httpResponse.headers().set(CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM.toString());
		}
		if (router.resourceType() == ResourceType.FILE) {
			httpResponse.headers().add(CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"", file.getName()));
		}
		if (HttpUtil.isKeepAlive(httpRequest)) {
			httpResponse.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		ctx.write(httpResponse);

		ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(file), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {

			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				log.info("File '{}' transfered completedly.", file.getName());
				Throwable e = future.cause();
				if (e != null) {
					log.error(e.getMessage(), e);
				}
			}

			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
				if (log.isTraceEnabled()) {
					if (total < 0) {
						log.trace("File '{}' transfering progress: {}", file.getName(), "0%");
					} else {
						log.trace("File '{}' transfering progress: {}", file.getName(),
								NumberUtils.format((double) progress / total, "0.#%"));
					}
				}
			}
		});
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!HttpUtil.isKeepAlive(httpRequest)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private Request makeRequest(HttpRequest httpRequest, Router router, String rawPath) {
		HttpHeaders httpHeaders = copyHttpHeaders(httpRequest);
		if (MapUtils.isNotEmpty(router.defaultHeaders())) {
			httpHeaders.addAll(router.defaultHeaders());
		}
		if (CollectionUtils.isNotEmpty(router.ignoredHeaders())) {
			MapUtils.removeKeys(httpHeaders, router.ignoredHeaders());
		}
		ByteBuf byteBuf = ((FullHttpRequest) httpRequest).content();
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
		return request;
	}

	protected HttpHeaders copyHttpHeaders(HttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

}
