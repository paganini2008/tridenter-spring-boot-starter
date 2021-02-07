package indi.atlantis.framework.seafloor.gateway;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
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
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyEmbeddedServer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class NettyEmbeddedServer implements EmbeddedServer {

	private final AtomicBoolean started = new AtomicBoolean(false);
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	@Autowired
	private RequestDispatcher httpRequestDispatcher;

	@Value("${spring.application.gateway.embeddedserver.port:7000}")
	private int port;

	@Value("${spring.application.gateway.embeddedserver.threads:8}")
	private int threadCount;

	@Value("${spring.application.gateway.embeddedserver.hostName:}")
	private String hostName;

	@Value("${spring.application.gateway.embeddedserver.idleTimeout:60}")
	private int idleTimeout;

	@Value("${spring.application.gateway.embeddedserver.maxContentLength:65536}")
	private int maxContentLength;

	@Value("${spring.application.gateway.embeddedserver.maxInitialLineLength:4096}")
	private int maxInitialLineLength;

	@Value("${spring.application.gateway.embeddedserver.maxHeaderSize:8192}")
	private int maxHeaderSize;

	@Value("${spring.application.gateway.embeddedserver.maxChunkSize:8192}")
	private int maxChunkSize;

	@Value("${spring.application.gateway.embeddedserver.cors:true}")
	private boolean corsEnabled;

	@Value("${spring.application.gateway.embeddedserver.cors.maxAge:0}")
	private long maxAge;

	@Value("${spring.application.gateway.embeddedserver.gzip:true}")
	private boolean gzipEnabled;

	@PostConstruct
	public int start() {
		if (isStarted()) {
			throw new IllegalStateException("Netty has been started.");
		}
		bossGroup = new NioEventLoopGroup(threadCount);
		workerGroup = new NioEventLoopGroup(threadCount);
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new IdleStateHandler(idleTimeout, 0, 0, TimeUnit.SECONDS));
				if (gzipEnabled) {
					pipeline.addLast(new HttpContentCompressor());
				}
				pipeline.addLast("httpServerCodec", new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize));
				pipeline.addLast("httpAggregator", new HttpObjectAggregator(maxContentLength));
				if (corsEnabled) {
					CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().allowedRequestHeaders("*")
							.allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).maxAge(maxAge)
							.build();
					pipeline.addLast(new CorsHandler(corsConfig));
				}
				pipeline.addLast((ChannelHandler) httpRequestDispatcher);
			}
		});
		try {
			InetSocketAddress socketAddress = StringUtils.isNotBlank(hostName) ? new InetSocketAddress(hostName, port)
					: new InetSocketAddress(port);
			bootstrap.bind(socketAddress).sync();
			started.set(true);
			log.info("Netty is started ok on port: " + socketAddress);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return port;
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
