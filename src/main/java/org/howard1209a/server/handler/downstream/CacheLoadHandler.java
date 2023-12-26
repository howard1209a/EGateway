package org.howard1209a.server.handler.downstream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import org.howard1209a.cache.CacheProvider;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.cache.pojo.PersistentResponse;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.pojo.HttpRequestWrapper;

public class CacheLoadHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequestWrapper wrapper = (HttpRequestWrapper) msg;
        CacheProvider<PersistentResponse> cacheProvider = ResponseCacheProvider.getInstance();
        PersistentResponse persistentResponse = cacheProvider.loadCache(wrapper.getRoute(), ResponseCacheProvider.FullHttpRequest2MD5(wrapper.getRequest()));

        if (persistentResponse == null) {
            super.channelRead(ctx, msg);
            return;
        }

        DefaultFullHttpResponse response = ResponseCacheProvider.PersistentResponse2DefaultFullHttpResponse(persistentResponse);
        Channel downStreamChannel = ctx.channel();
        ChannelFuture channelFuture = downStreamChannel.writeAndFlush(response);
        KeepaliveManager.getInstance().handleDownStreamChannelClose(downStreamChannel, channelFuture);
    }
}
