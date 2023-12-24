package org.howard1209a.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Gateway;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.Server;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.dispatcher.Dispatcher;
import org.howard1209a.server.dispatcher.HashDispatcher;
import org.howard1209a.server.dispatcher.PollingDispatcher;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class RouteHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof LastHttpContent) { // 处理一个空的LastHttpContent
            return;
        }
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

        // 如果是第一次route，那么需要根据route配置中的负载均衡策略来初始化相应的dispatcher
        DispatchHandler dispatchHandler = (DispatchHandler) ctx.pipeline().get("DispatchHandler");
        if (dispatchHandler.getDispatcher() == null) {
            Dispatcher dispatcher = null;
            String loadbalanceMathod = route.getLoadBalance();
            if (loadbalanceMathod.equals("polling")) {
                dispatcher = PollingDispatcher.getInstance();
            } else if (loadbalanceMathod.equals("hash")) {
                dispatcher = HashDispatcher.getInstance();
            }
            dispatchHandler.setDispatcher(dispatcher);
        }

        HttpRequestWrapper wrapper = new HttpRequestWrapper(httpRequest, route, downStreamChannel);
        super.channelRead(ctx, wrapper);
    }
}
