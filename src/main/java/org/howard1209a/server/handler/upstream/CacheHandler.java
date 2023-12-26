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

public class CacheHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        Channel upStreamChannel = ctx.channel();
        StreamManager streamManager = StreamManager.getInstance();
        Route route = streamManager.getRoute(upStreamChannel);
        FullHttpRequest request = streamManager.getFullHttpRequest(upStreamChannel);
        CacheProvider<PersistentResponse> cacheProvider = ResponseCacheProvider.getInstance();
        cacheProvider.saveCache(route, ResponseCacheProvider.FullHttpRequest2MD5(request), ResponseCacheProvider.DefaultFullHttpResponse2PersistentResponse(response));
        super.channelRead(ctx, msg);
    }
}
