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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.StringUtils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyHttpRequestDispatcher
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Sharable
@Slf4j
public class NettyHttpRequestDispatcher extends ChannelInboundHandlerAdapter {

	@Autowired
	private RouterManager routerManager;

	@Qualifier("staticResourceResolver")
	@Autowired
	private NettyHttpObjectResolver staticResourceResolver;

	@Qualifier("httpRequestResolver")
	@Autowired
	private NettyHttpObjectResolver dynamicResourceResolver;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		if (!(data instanceof HttpObject)) {
			return;
		}
		if (data instanceof HttpRequest) {
			final HttpRequest httpRequest = (HttpRequest) data;
			final String path = httpRequest.uri();
			Router router = routerManager.match(path);
			String url = router.url();
			if (StringUtils.isNotBlank(url)) {
				staticResourceResolver.resolve(httpRequest, router, url, ctx);
			} else {
				String rawPath = router.trimPath(path);
				CharSequence mineType = HttpUtil.getMimeType(httpRequest);
				if (StringUtils.isNotBlank(mineType) && mineType.toString().contains("multipart/form-data")) {
					ctx.fireChannelRead(data);
				} else {
					dynamicResourceResolver.resolve(httpRequest, router, rawPath, ctx);
				}
			}
		} else {
			ctx.fireChannelRead(data);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		super.exceptionCaught(ctx, e);
		log.error(e.getMessage(), e);
		ctx.channel().close();
	}

}
