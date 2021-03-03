package indi.atlantis.framework.tridenter.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.StringUtils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * HttpRequestDispatcher
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Sharable
@Slf4j
public class HttpRequestDispatcher extends ChannelInboundHandlerAdapter {

	@Autowired
	private RouterManager routerManager;

	@Qualifier("staticResourceResolver")
	@Autowired
	private ResourceResolver staticResourceResolver;

	@Qualifier("dynamicResourceResolver")
	@Autowired
	private ResourceResolver dynamicResourceResolver;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
		final FullHttpRequest httpRequest = (FullHttpRequest) data;
		final String path = httpRequest.uri();
		Router router = routerManager.match(path);
		String url = router.url();
		if (StringUtils.isNotBlank(url)) {
			staticResourceResolver.resolve(httpRequest, router, url, ctx);
		} else {
			String rawPath = router.trimPath(path);
			dynamicResourceResolver.resolve(httpRequest, router, rawPath, ctx);
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		super.exceptionCaught(ctx, e);
		log.error(e.getMessage(), e);
		ctx.channel().close();
	}

}
