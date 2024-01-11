package org.howard1209a.server.handler.upstream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.howard1209a.server.StreamManager;

public class HeaderAddHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        Channel upStreamChannel = ctx.channel();

        checkCacheStateHeader(response, upStreamChannel);

        super.channelRead(ctx, msg);
    }

    private void checkCacheStateHeader(FullHttpResponse response, Channel upStreamChannel) {
        StreamManager streamManager = StreamManager.getInstance();
        HttpHeaders headers = response.headers();
        if (streamManager.isNotCache(upStreamChannel)) {
            headers.add("cacheState", "MISS");
        } else {
            headers.add("cacheState", "EXPIRED");
        }
    }

    public static void addCacheStateHitHeader(FullHttpResponse response) {
        HttpHeaders headers = response.headers();
        headers.add("cacheState", "HIT");
    }
}
