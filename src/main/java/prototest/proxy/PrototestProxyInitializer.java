package prototest.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


public class PrototestProxyInitializer extends ChannelInitializer<SocketChannel>
{
	private Integer outboundPort;

	public PrototestProxyInitializer(final Integer outboundPort)
	{
		this.outboundPort = outboundPort;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ch.pipeline().addLast(
				new PrototestProxyInboundHandler(outboundPort));
	}
}


