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

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;

import indi.atlantis.framework.tridenter.gateway.EmbeddedServerProperties.Netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyEmbeddedServer
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class NettyEmbeddedServer implements EmbeddedServer {

	private final AtomicBoolean started = new AtomicBoolean(false);
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	@Autowired
	private EmbeddedServerProperties serverProperties;

	@Autowired
	private NettyHttpRequestDispatcher httpRequestDispatcher;

	@Autowired
	private NettyMultiPartHandler multiPartHandler;

	@PostConstruct
	public int start() {
		if (isStarted()) {
			throw new IllegalStateException("Netty has been started.");
		}
		Netty netty = serverProperties.getNetty();
		bossGroup = new NioEventLoopGroup(netty.getBossGroupThreads());
		workerGroup = new NioEventLoopGroup(netty.getWorkGroupThreads());
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new IdleStateHandler(netty.getIdleTimeout(), 0, 0, TimeUnit.SECONDS));
				if (netty.isGzipEnabled()) {
					pipeline.addLast(new HttpContentCompressor());
				}
				pipeline.addLast("httpDecoder",
						new HttpRequestDecoder(netty.getMaxInitialLineLength(), netty.getMaxHeaderSize(), netty.getMaxChunkSize()));
				pipeline.addLast("httpAggregator", new HttpObjectAggregator(netty.getMaxContentLength()));
				ch.pipeline().addLast("httpEncoder", new HttpResponseEncoder());
				pipeline.addLast(new ChunkedWriteHandler());
				if (netty.isCorsEnabled()) {
					CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().allowedRequestHeaders("*")
							.allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
							.maxAge(netty.getMaxAge()).build();
					pipeline.addLast(new CorsHandler(corsConfig));
				}
				pipeline.addLast(httpRequestDispatcher);
				pipeline.addLast(multiPartHandler);
			}
		});
		InetSocketAddress socketAddress = StringUtils.isNotBlank(serverProperties.getHostName())
				? new InetSocketAddress(serverProperties.getHostName(), serverProperties.getPort())
				: new InetSocketAddress(serverProperties.getPort());
		try {
			bootstrap.bind(socketAddress).sync();
			started.set(true);
			log.info("Netty is started ok on port: " + socketAddress);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return socketAddress.getPort();
	}

	@PreDestroy
	public void stop() {
		if (!isStarted()) {
			return;
		}
		try {
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
			}
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
			}
			started.set(false);
			log.info("Netty is stoped ok.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean isStarted() {
		return started.get();
	}

}
