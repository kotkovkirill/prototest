package prototest.proxy;

import java.util.Random;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;


public class PrototestProxyInboundHandler extends ChannelInboundHandlerAdapter
{

	private static final boolean ENABLE_PACKET_CORRUPTION = true;
	private static final double CORRUPTION_PROBABILITY = 0.00002;
	private Integer outboundPort;
	private Channel outboundChannel;

	public PrototestProxyInboundHandler(final Integer outboundPort)
	{
		this.outboundPort = outboundPort;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		final Channel inboundChannel = ctx.channel();

		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop())
				.channel(ctx.channel().getClass())
				.handler(new PrototestProxyOutboundHandler(inboundChannel))
				.option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = b.connect("localhost", outboundPort);
		outboundChannel = f.channel();

		f.addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				inboundChannel.read();
			} else {
				inboundChannel.close();
			}
		});
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
		if (outboundChannel.isActive()) {

			ByteBuf buff = (ByteBuf)msg;

			ByteBuf outputBuff = ctx.alloc().buffer(buff.readableBytes());

			try {

				while(buff.isReadable()) {

					outputBuff.writeByte( corrupt (buff.readByte()));

				}

				outboundChannel.writeAndFlush(outputBuff).addListener((ChannelFutureListener) future -> {
					if (future.isSuccess()) {
						ctx.channel().read();
					} else {
						future.channel().close();
					}
				});

			} finally
			{
				//ReferenceCountUtil.release(outputBuff);
			}
		}
	}

	private Random randomGenerator = new Random();

	private int corrupt(final byte b)
	{
		if(ENABLE_PACKET_CORRUPTION && randomGenerator.nextDouble() < CORRUPTION_PROBABILITY) {
			return 0;
		} else {
			return b;
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		closeOnFlush(ctx.channel());
	}

	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
}

