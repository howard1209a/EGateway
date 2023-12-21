package org.howard1209a.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpResponse;
import org.howard1209a.server.StreamManager;

public class DistributeHandler extends SimpleChannelInboundHandler<HttpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpResponse httpResponse) throws Exception {
        Channel upStreamChannel = channelHandlerContext.channel();
        Channel downStreamChannel = StreamManager.getInstance().get(upStreamChannel);
        StreamManager.getInstance().remove(upStreamChannel);
        upStreamChannel.close();

        ChannelFuture distributeFuture = downStreamChannel.writeAndFlush(httpResponse);
        distributeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.channel().close();
            }
        });
    }
}
