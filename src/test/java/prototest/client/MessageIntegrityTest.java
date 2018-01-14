package prototest.client;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import prototest.Prototest;
import prototest.server.PrototestServerHandler;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.Random;


@Ignore
public class MessageIntegrityTest
{
	@Autowired
	private PrototestClientHandler prototestClientHandler;
	@Autowired
	private PrototestServerHandler prototestServerHandler;
	@Value("${server.inbound_port}")
	private Integer port;
	@Autowired
	private MessagePersister messagePersister;
	@Autowired
	private SslContext clientSsl;
	@Autowired
	private SslContext serverSsl;


	@Test
	public void integrityTestSuccess() {





		EmbeddedChannel clientChannel = setupClientChannel();

		prototestClientHandler.invokeServer("Test body");

		ByteBuf clientRequest = clientChannel.readOutbound();

		System.out.println("Request from client : " + clientRequest.toString(Charset.defaultCharset()));

		ByteBuf clientRequestCorrupted = corrupt(clientRequest);

		System.out.println("Corrupted request from client : " + clientRequestCorrupted.toString(Charset.defaultCharset()));

		EmbeddedChannel serverChannel = setupServerChannel();

		serverChannel.writeInbound(clientRequestCorrupted);

//		ByteBuf serverResponse = serverChannel.readOutbound();
//
//
//
//		verify(messagePersister, times(0)).delete(any());
//
//
//
//		clientChannel.writeInbound(serverResponse);
//
//		verify(messagePersister, times(1)).delete(any());
	}

	private ByteBuf corrupt(final ByteBuf clientRequest)
	{
		byte[] data = new byte[clientRequest.readableBytes()];

		clientRequest.getBytes(clientRequest.readerIndex(), data);

		//data[data.length-1] = RandomUtils.nextBytes(1)[0];

		return Unpooled.copiedBuffer(data);

	}

	private EmbeddedChannel setupServerChannel()
	{
		EmbeddedChannel channel = new EmbeddedChannel();
		ChannelPipeline pipeline = channel.pipeline();

		if (serverSsl != null)
		{
			pipeline.addLast(serverSsl.newHandler(channel.alloc()));
		}

		pipeline.addLast(new ProtobufVarint32FrameDecoder());
		pipeline.addLast(new ProtobufDecoder(Prototest.ProtoRequest.getDefaultInstance()));

		pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast(new ProtobufEncoder());

		pipeline.addLast(prototestServerHandler);

		return channel;
	}

	private EmbeddedChannel setupClientChannel()
	{
		EmbeddedChannel channel = new EmbeddedChannel();

		ChannelPipeline pipeline = channel.pipeline();

		if (clientSsl != null)
		{
//			pipeline.addLast(clientSsl.newHandler(channel.alloc(), "localhost", port));
			pipeline.addLast(clientSsl.newHandler(channel.alloc()));
		}

		pipeline.addLast(new ProtobufVarint32FrameDecoder());
		pipeline.addLast(new ProtobufDecoder(Prototest.ProtoResponse.getDefaultInstance()));

		pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast(new ProtobufEncoder());

		pipeline.addLast(prototestClientHandler);

		prototestClientHandler.setChannel(channel);
		return channel;
	}

}
