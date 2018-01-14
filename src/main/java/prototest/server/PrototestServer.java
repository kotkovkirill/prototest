package prototest.server;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


@Component
public class PrototestServer
{

	private InternalLogger logger = InternalLoggerFactory.getInstance(PrototestServer.class);

	@Value("${server.inbound_port}")
	private Integer port;

	private EventLoopGroup bossGroup = new NioEventLoopGroup(1);

	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	@Autowired
	private PrototestServerInitializer prototestServerInitializer;

	public void start()
	{
		try {


			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(prototestServerInitializer);

			logger.info("Starting server");

			b.bind(port).sync();

			logger.info("Server started");


		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@PreDestroy
	public void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
}
