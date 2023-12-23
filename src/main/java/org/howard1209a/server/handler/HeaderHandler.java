package org.howard1209a.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.pojo.HttpRequestWrapper;

public class HeaderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequestWrapper wrapper = (HttpRequestWrapper) msg;
        HttpRequest request = wrapper.getRequest();

        processConnectionHeader(request, ctx);

        super.channelRead(ctx, msg);
    }

    private void processConnectionHeader(HttpRequest request, ChannelHandlerContext ctx) {
        // 如果是第二次及之后到来的长连接，不做处理
        KeepaliveManager keepaliveManager = KeepaliveManager.getInstance();
        if (keepaliveManager.get(ctx.channel()) != null) {
            return;
        }

        // 如果是第一次到来的连接，处理
        HttpHeaders headers = request.headers();
        String connection = headers.get("Connection");
        if (connection != null) {
            if (connection.equals("keep-alive")) {
                keepaliveManager.register(ctx.channel());
            }
        } else {
            if (request.protocolVersion().equals(HttpVersion.HTTP_1_1)) {
                keepaliveManager.register(ctx.channel());
            }
        }
    }
}
