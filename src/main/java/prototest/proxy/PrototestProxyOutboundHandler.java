package prototest.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class PrototestProxyOutboundHandler extends ChannelInboundHandlerAdapter
{
	private final Channel inboundChannel;

	public PrototestProxyOutboundHandler(Channel inboundChannel) {
		this.inboundChannel = inboundChannel;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.read();
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
		inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				ctx.channel().read();
			} else {
				future.channel().close();
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		PrototestProxyInboundHandler.closeOnFlush(inboundChannel);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		PrototestProxyInboundHandler.closeOnFlush(ctx.channel());
	}
}
