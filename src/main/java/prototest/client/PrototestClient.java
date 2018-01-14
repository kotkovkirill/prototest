package prototest.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;


@Component
public class PrototestClient
{
	private static final int CONNECTION_RETRIES = 10;
	private InternalLogger logger = InternalLoggerFactory.getInstance(PrototestClient.class);
	private EventLoopGroup group = new NioEventLoopGroup();
	private Bootstrap bootstrap;
	private Channel channel;
	private PrototestClientHandler handler;

	@Value("${client.port}")
	private Integer port;

	@Autowired
	private PrototestClientInitializer prototestClientInitializer;


	@PostConstruct
	public void setup()
	{
		bootstrap = new Bootstrap();
		bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.handler(prototestClientInitializer);

	}

	@PreDestroy
	public void shutdown() {
		group.shutdownGracefully();
	}

	public void sendRequest(final String body)
	{
		PrototestClientHandler handler = handler();

		handler.invokeServer(body);

	}

	private PrototestClientHandler handler() {
		if(channelInactive()) {
			logger.info("Acquiring hanlder");
			handler = connect().pipeline().get(PrototestClientHandler.class);
		}
		return handler;
	}

	private Channel connect()
	{
		if(channelInactive()) {

			logger.info("Acquiring connection");

			for(int i = 0; i < CONNECTION_RETRIES; i++) {
				try {

					channel = bootstrap.connect("localhost", port).sync().channel();
					logger.info("Connection successful");
					break;

				} catch (InterruptedException e) {

					logger.error("Connection failed, retrying");
					try { Thread.currentThread().sleep(3000); } catch (InterruptedException e1) {}

				}
			}

			if(channelInactive()) throw new RuntimeException("Connection failed, maximum number of tried reached");

		}

		return channel;
	}

	private boolean channelInactive() {
		return channel == null || !channel.isActive();
	}
}


