package org.howard1209a.server.handler.upstream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.howard1209a.cache.CacheProvider;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.cache.pojo.PersistentResponse;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.handler.downstream.CacheLoadHandler;

public class CacheSaveHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        Channel upStreamChannel = ctx.channel();

        StreamManager streamManager = StreamManager.getInstance();
        Route route = streamManager.getRoute(upStreamChannel);
        FullHttpRequest request = streamManager.getFullHttpRequest(upStreamChannel);

        CacheProvider<PersistentResponse> cacheProvider = ResponseCacheProvider.getInstance();
        PersistentResponse persistentResponse = ResponseCacheProvider.DefaultFullHttpResponse2PersistentResponse(response);
        String key = ResponseCacheProvider.FullHttpRequest2MD5(request);
        cacheProvider.saveCache(route, key, persistentResponse);
        CacheLoadHandler.unlock(key);

        super.channelRead(ctx, msg);
    }
}
