package prototest.proxy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


@Component
public class PrototestProxy {

	@Value("${proxy.inbound_port}")
	private Integer inboundPort;
	@Value("${proxy.outbound_port}")
	private Integer outboundPort;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;

	public void start()
	{
		try {

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new PrototestProxyInitializer(outboundPort))
					.childOption(ChannelOption.AUTO_READ, false)
					.bind(inboundPort).sync();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostConstruct
	public void init() {
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
	}

	@PreDestroy
	public void shutdown() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();

	}
}
