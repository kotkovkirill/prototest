package prototest.server;

import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;


@ChannelHandler.Sharable
@Component
public class PrototestServerAuthHandler extends SimpleChannelInboundHandler<Prototest.ProtoRequest>
{

	@Value("${secret_phrase}")
	private String secret;
	private String md5;


	private InternalLogger logger = InternalLoggerFactory.getInstance(PrototestServerAuthHandler.class);

	@PostConstruct
	public void setup() {
		md5 = DigestUtils.md5DigestAsHex(secret.getBytes(Charset.forName("UTF-8")));
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Prototest.ProtoRequest request) throws Exception {

		if(md5.equals(request.getAuthHeader())) {

			logger.info("Client authenticated");
			ctx.fireChannelRead(request);

		} else {

			logger.info("Client not authenticated");
			Prototest.ProtoResponse.Builder builder = Prototest.ProtoResponse.newBuilder();
			builder.setId(request.getId());
			builder.setResponseStatus(Prototest.ResponseStatus.ERROR);
			ctx.write(builder.build());

		}
	}
}

