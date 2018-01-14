package prototest.client;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;
import prototest.server.PrototestServerHandler;


@Component
@Scope("prototype")
public class PrototestClientHandler extends SimpleChannelInboundHandler<Prototest.ProtoResponse>
{
	InternalLogger logger = InternalLoggerFactory.getInstance(PrototestClientHandler.class);


	@Value("${secret_phrase}")
	private String secret;

	private volatile Channel channel;

	@Autowired
	private MessagePersister messagePersister;
	private String md5;

	public PrototestClientHandler() {
		super(false);
	}

	@PostConstruct
	public void setup() {
		md5 = DigestUtils.md5DigestAsHex(secret.getBytes(Charset.forName("UTF-8")));
	}

	public void invokeServer(final String body) {

		if(channel == null || !channel.isActive()) {
			throw new RuntimeException("Channel inactive");
		}

		Prototest.ProtoRequest.Builder builder = Prototest.ProtoRequest.newBuilder();

		builder.setId(UUID.randomUUID().toString());
		builder.setBody(body);
		builder.setAuthHeader(md5);

		messagePersister.save(builder.build());

		for(Prototest.ProtoRequest message : messagePersister.getMessages()) {
			logger.info("Sending message : " + message.getId());
			channel.writeAndFlush(message);
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx)
	{
		setChannel(ctx.channel());
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Prototest.ProtoResponse resp) throws Exception {
		if(Prototest.ResponseStatus.SUCCESS.equals(resp.getResponseStatus())) {
			messagePersister.delete(resp);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void setChannel(final Channel channel)
	{
		this.channel = channel;
	}
}


