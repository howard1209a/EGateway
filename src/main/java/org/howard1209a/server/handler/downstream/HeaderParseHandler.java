package org.howard1209a.server.handler.downstream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.pojo.HttpRequestWrapper;

public class HeaderParseHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequestWrapper wrapper = (HttpRequestWrapper) msg;
        FullHttpRequest request = wrapper.getRequest();

        processConnectionHeader(request, ctx);
        processCacheHeader(request, wrapper);
        super.channelRead(ctx, msg);
    }

    private void processCacheHeader(HttpRequest request, HttpRequestWrapper wrapper) {
        HttpHeaders headers = request.headers();
        String forceNotCache = headers.get("forceNotCache");
        // 如果本次请求设置了强制不缓存或没有设置强制不缓存但是对应route没有配置缓存，则本次请求将与缓存无关
        if (forceNotCache != null || wrapper.getRoute().getCache() == null) {
            wrapper.setNotCache(true);
        }
    }

    private void processConnectionHeader(HttpRequest request, ChannelHandlerContext ctx) {
        // 如果已经被KeepaliveManager管理，说明此次是长连接的请求
        KeepaliveManager keepaliveManager = KeepaliveManager.getInstance();
        if (keepaliveManager.get(ctx.channel()) != null) {
            return;
        }

        // 如果未被KeepaliveManager管理，则检查此次请求是否是长连接
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
