package org.howard1209a.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Gateway;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.Server;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class RouteHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequest httpRequest = (HttpRequest) msg;
        Gateway info = ServerConfiguration.getInfo();
        Route route = info.matchRoute(httpRequest.uri());
        Channel downStreamChannel = ctx.channel();

        if (route == null) {
            log.warn("404 not found");
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.NOT_FOUND);
            response.headers().setInt(CONTENT_LENGTH, 0);
            ChannelFuture writeFuture = downStreamChannel.writeAndFlush(response);
            writeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    channelFuture.channel().close();
                }
            });
            return;
        }

        HttpRequestWrapper wrapper = new HttpRequestWrapper(httpRequest, route, downStreamChannel);
        super.channelRead(ctx, wrapper);
    }
}
