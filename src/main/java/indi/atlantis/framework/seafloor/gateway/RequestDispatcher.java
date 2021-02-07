package indi.atlantis.framework.seafloor.gateway;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RequestDispatcher
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@Slf4j
public class RequestDispatcher extends ChannelInboundHandlerAdapter {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		super.exceptionCaught(ctx, e);
		log.error(e.getMessage(), e);
		ctx.channel().close();
	}

}
