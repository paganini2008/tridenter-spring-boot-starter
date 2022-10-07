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
package io.atlantisframework.tridenter.gateway;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

import io.atlantisframework.tridenter.http.ForwardedRequest;
import io.atlantisframework.tridenter.http.RequestTemplate;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyMultiPartHandler
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Slf4j
@Sharable
public class NettyMultiPartHandler extends SimpleChannelInboundHandler<HttpObject> {

	public NettyMultiPartHandler() {
		super(false);
	}

	private HttpDataFactory factory = new DefaultHttpDataFactory(true);
	private HttpPostRequestDecoder decoder;
	private HttpRequest httpRequest;

	@Autowired
	private RouterManager routerManager;

	@Autowired
	private RequestTemplate requestTemplate;

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject) throws Exception {
		if (httpObject instanceof HttpRequest) {
			httpRequest = (HttpRequest) httpObject;
			decoder = new HttpPostRequestDecoder(factory, httpRequest);
			decoder.setDiscardThreshold(0);
		}
		if (httpObject instanceof HttpContent) {
			if (decoder != null) {
				HttpContent chunk = (HttpContent) httpObject;
				decoder.offer(chunk);
				if (chunk instanceof LastHttpContent) {
					writeChunk(ctx);
					decoder.destroy();
					decoder = null;
				}
				ReferenceCountUtil.release(httpObject);
			} else {
				ctx.fireChannelRead(httpObject);
			}
		}
	}

	private void doRequest(FileUpload fileUpload, ChannelHandlerContext ctx) throws IOException {
		final String path = httpRequest.uri();
		Router router = routerManager.match(path);
		String rawPath = router.trimPath(path);
		HttpHeaders httpHeaders = copyHttpHeaders(httpRequest);
		if (MapUtils.isNotEmpty(router.defaultHeaders())) {
			httpHeaders.addAll(router.defaultHeaders());
		}
		if (CollectionUtils.isNotEmpty(router.ignoredHeaders())) {
			MapUtils.removeKeys(httpHeaders, router.ignoredHeaders());
		}

		ForwardedRequest request = new ForwardedRequest(rawPath, HttpMethod.valueOf(httpRequest.method().name()), httpHeaders);
		MultiValueMap<String, Object> resultMap = new LinkedMultiValueMap<>();
		resultMap.add("file", new InnerInputStreamResource(new FileInputStream(fileUpload.getFile()), fileUpload));
		request.setBody(resultMap);
		request.setTimeout(router.timeout());
		request.setRetries(router.retries());
		request.setAllowedPermits(router.allowedPermits());

		ResponseEntity<String> responseEntity = requestTemplate.sendRequest(router.provider(), request, String.class);
		String body = responseEntity.getBody();
		HttpStatus httpStatus = responseEntity.getStatusCode();
		httpHeaders = responseEntity.getHeaders();

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

	protected HttpHeaders copyHttpHeaders(HttpRequest httpRequest) {
		HttpHeaders headers = new HttpHeaders();
		for (Map.Entry<String, String> headerEntry : httpRequest.headers()) {
			headers.add(headerEntry.getKey(), headerEntry.getValue());
		}
		return headers;
	}

	private void writeChunk(ChannelHandlerContext ctx) throws IOException {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null && HttpDataType.FileUpload.equals(data.getHttpDataType())) {
					final FileUpload fileUpload = (FileUpload) data;
					doRequest(fileUpload, ctx);
				}
			}
		} catch (EndOfDataDecoderException ignored) {
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		log.error(e.getMessage(), e);
		ctx.channel().close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}

	private static class InnerInputStreamResource extends InputStreamResource {

		public InnerInputStreamResource(InputStream inputStream, FileUpload fileUpload) {
			super(inputStream);
			this.fileUpload = fileUpload;
		}

		private final FileUpload fileUpload;

		@Override
		public long contentLength() throws IOException {
			return fileUpload.length();
		}

		@Override
		public String getFilename() {
			return fileUpload.getFilename();
		}

	}
}
