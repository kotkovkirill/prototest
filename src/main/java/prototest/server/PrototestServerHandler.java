package prototest.server;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import prototest.Prototest;

@Component
@Scope("prototype")
public class PrototestServerHandler extends SimpleChannelInboundHandler<Prototest.ProtoRequest>
{

	InternalLogger logger = InternalLoggerFactory.getInstance(PrototestServerHandler.class);


	@Override
	public void channelRead0(ChannelHandlerContext ctx, Prototest.ProtoRequest request) throws Exception {

		logger.info("Server got request : " + request.getBody());

		Prototest.ProtoResponse.Builder builder = Prototest.ProtoResponse.newBuilder();
		builder.setId(request.getId());
		builder.setResponseStatus(Prototest.ResponseStatus.SUCCESS);

		ctx.write(builder.build());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Server got exception : " + cause.getMessage());
		ctx.close();
	}



}
