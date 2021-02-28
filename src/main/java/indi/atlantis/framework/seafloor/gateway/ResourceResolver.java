package indi.atlantis.framework.seafloor.gateway;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 
 * ResourceResolver
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface ResourceResolver {

	void resolve(FullHttpRequest httpRequest, Router router, String path, ChannelHandlerContext ctx) throws Exception;

}
