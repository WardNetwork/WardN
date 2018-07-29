package netty;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TimeClientHandler extends ChannelInboundHandlerAdapter  {

//	@Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println("Sendiung");
//		ctx.writeAndFlush("asd");
//		ctx.flush();
//	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf b = (ByteBuf)msg;
		
		try {
			long currentTimeMillis = (b.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis).getTime());
            System.out.println(new Date(System.currentTimeMillis()).getTime());
            ctx.writeAndFlush("Testmessage ICO");
            ctx.close();
//            String s = b.toString();
//            System.out.println(s);
//            ctx.close();
        } finally {
            b.release();
        }
		
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
