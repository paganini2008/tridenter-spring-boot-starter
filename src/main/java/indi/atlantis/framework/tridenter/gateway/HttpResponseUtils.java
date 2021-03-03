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
 * @author Jimmy Hoff
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
