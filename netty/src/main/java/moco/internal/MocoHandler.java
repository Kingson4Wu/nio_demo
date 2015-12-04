package moco.internal;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import static io.netty.channel.ChannelHandler.Sharable;
import static io.netty.handler.codec.http.HttpHeaders.*;

@Sharable
public class MocoHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest message) throws Exception {
        //FullHttpResponse response = handleRequest(message);

//-----------
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.valueOf(message.getProtocolVersion().text()),
                HttpResponseStatus.OK);
        //response.content().writeBytes(message.content());
        response.content().writeBytes(message.getUri().getBytes());
        //response.content().writeBytes("kxw".getBytes());
//-----------
        prepareForKeepAlive(message, response);
        closeIfNotKeepAlive(message, ctx.write(response));
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    private void closeIfNotKeepAlive(final FullHttpRequest request, final ChannelFuture future) {
        if (!isKeepAlive(request)) {//kxw
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void prepareForKeepAlive(final FullHttpRequest request, final FullHttpResponse response) {
        if (isKeepAlive(request)) {
            setKeepAlive(response, true);
            setContentLengthForKeepAlive(response);
        }
    }

    private void setContentLengthForKeepAlive(final FullHttpResponse response) {
        if (!isContentLengthSet(response)) {
            setContentLength(response, response.content().writerIndex());
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
    }

   /* public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) {
        //monitor.onException(cause);
        if(cause instanceof IOException){
            System.out.println("kxw");
        }else{
            cause.printStackTrace();//捕捉异常信息
        }
        ctx.close();//出现异常时关闭channel
    }*/

}
