package indi.atlantis.framework.seafloor.gateway;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;

import indi.atlantis.framework.seafloor.http.ResponseExchanger;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyChannelProgressiveResponseExchanger implements ResponseExchanger<Void> {

	private final ChannelHandlerContext ctx;

	public NettyChannelProgressiveResponseExchanger(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public Void exchange(ClientHttpResponse response) {
		try {
			ChannelFuture channelFuture = ctx.write(new ChunkedStream(response.getBody()));
			channelFuture.addListener(new ChannelProgressiveFutureListener() {

				@Override
				public void operationComplete(ChannelProgressiveFuture future) throws Exception {
					future.channel().close();

				}

				@Override
				public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
					if (log.isTraceEnabled()) {
						log.trace("Current {}/{}", progress, total);
					}

				}
			});
			ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
