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

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.github.paganini2008.devtools.StringUtils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * 
 * HttpResponseUtils
 * @author Fred Feng
 *
 * @version 1.0
 */
public abstract class HttpResponseUtils {

	public static void sendRedirect(ChannelHandlerContext ctx, String newUrl) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUrl);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String msg) {
		String content = StringUtils.isNotBlank(msg) ? msg : status.toString();
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}
