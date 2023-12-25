package org.howard1209a.server.handler.downstream;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Gateway;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.dispatcher.Dispatcher;
import org.howard1209a.server.dispatcher.HashDispatcher;
import org.howard1209a.server.dispatcher.PollingDispatcher;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class RouteHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;

        Gateway info = ServerConfiguration.getInfo();
        Route route = info.matchRoute(httpRequest.uri()); // 匹配route
        Channel downStreamChannel = ctx.channel();

        if (route == null) { // 处理404
            handleNotFound(httpRequest, downStreamChannel);
            return;
        }

        updateDispatcher(ctx, route); // 更新负载均衡器

        HttpRequestWrapper wrapper = new HttpRequestWrapper(httpRequest, route, downStreamChannel);
        super.channelRead(ctx, wrapper);
    }

    private void handleNotFound(FullHttpRequest httpRequest, Channel downStreamChannel) {
        log.warn("404 not found");
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.NOT_FOUND);
        response.headers().setInt(CONTENT_LENGTH, 0);

        ChannelFuture writeFuture = downStreamChannel.writeAndFlush(response); // 返回404响应
        writeFuture.addListener(new ChannelFutureListener() { // 关流
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.channel().close();
            }
        });
    }

    private void updateDispatcher(ChannelHandlerContext ctx, Route route) { // 根据route配置中的负载均衡策略来切换相应的dispatcher
        DispatchHandler dispatchHandler = (DispatchHandler) ctx.pipeline().get("DispatchHandler");
        Dispatcher dispatcher = null;
        String loadbalanceMathod = route.getLoadBalance();
        if (loadbalanceMathod.equals("polling")) {
            dispatcher = PollingDispatcher.getInstance();
        } else if (loadbalanceMathod.equals("hash")) {
            dispatcher = HashDispatcher.getInstance();
        }
        dispatchHandler.setDispatcher(dispatcher);
    }
}
