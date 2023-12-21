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

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class RouteHandler extends SimpleChannelInboundHandler<HttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
        Gateway info = ServerConfiguration.getInfo();
        Route route = info.matchRoute(httpRequest.uri());
        Channel downStreamChannel = channelHandlerContext.channel();
        if (route == null) {
            log.warn("404 error");
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.NOT_FOUND);
            response.headers().setInt(CONTENT_LENGTH, 0);
            downStreamChannel.writeAndFlush(response);
            return;
        }
        ChannelFuture connectFuture = Server.getInstance().connect(route.getIp(), Integer.parseInt(route.getPort()));
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel upStreamChannel = channelFuture.channel();
                StreamManager.getInstance().put(upStreamChannel, downStreamChannel);
                upStreamChannel.writeAndFlush(httpRequest);
            }
        });
    }
}
