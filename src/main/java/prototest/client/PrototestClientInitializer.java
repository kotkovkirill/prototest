package prototest.client;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import prototest.Prototest;


@Component
public class PrototestClientInitializer extends ChannelInitializer<SocketChannel> implements ApplicationContextAware
{
	@Resource(name="clientSsl")
	private SslContext sslCtx;
	@Value("${server.inbound_port}")
	private Integer port;
	@Value("${client.enable_taffic_shaping}")
	private Boolean trafficShapingEnaled;

	private ApplicationContext applicationContext;

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc(), "localhost", port));
		}

		p.addLast(new ProtobufVarint32FrameDecoder());

		p.addLast(new ProtobufDecoder(Prototest.ProtoResponse.getDefaultInstance()));

		if(trafficShapingEnaled) {
			p.addLast(new ChannelTrafficShapingHandler(1024, 1024 , 1000));
		}

		p.addLast(new ProtobufVarint32LengthFieldPrepender());

		p.addLast(new ProtobufEncoder());

		p.addLast(applicationContext.getBean(PrototestClientHandler.class));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}



