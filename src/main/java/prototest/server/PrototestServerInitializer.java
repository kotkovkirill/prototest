package prototest.server;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import prototest.Prototest;


@Component
public class PrototestServerInitializer extends ChannelInitializer<SocketChannel> implements ApplicationContextAware
{
	@Resource(name="serverSsl")
	private SslContext sslCtx;
	@Autowired
	private PrototestServerAuthHandler prototestServerAuthHandler;

	private ApplicationContext applicationContext;



	protected void initChannel(final SocketChannel socketChannel) throws Exception
	{
		ChannelPipeline p = socketChannel.pipeline();
		if (sslCtx != null) {
			SslHandler s = sslCtx.newHandler(socketChannel.alloc());
			p.addLast(s);
		}

		p.addLast(new ProtobufVarint32FrameDecoder());

		p.addLast(new ProtobufDecoder(Prototest.ProtoRequest.getDefaultInstance()));

		p.addLast(new ProtobufVarint32LengthFieldPrepender());

		p.addLast(new ProtobufEncoder());

		p.addLast(prototestServerAuthHandler);

		p.addLast(applicationContext.getBean(PrototestServerHandler.class));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
