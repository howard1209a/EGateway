package org.howard1209a.server.handler.downstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class FullHttpRequestAggregator extends ChannelInboundHandlerAdapter {
    private HttpRequest httpRequest;
    private ByteBuf requestData;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { // 聚合出完整的DefaultFullHttpRequest
        if (msg instanceof HttpRequest) { // 请求行和请求头信息
            httpRequest = (HttpRequest) msg;
            requestData = ByteBufAllocator.DEFAULT.heapBuffer();
        } else if (msg instanceof HttpContent) { // 请求体信息
            HttpContent httpContent = (HttpContent) msg;
            requestData.writeBytes(httpContent.content());
            if (msg instanceof LastHttpContent) {
                FullHttpRequest newRequest = generateRequest();
                super.channelRead(ctx, newRequest);
            }
        }
    }

    private FullHttpRequest generateRequest() { // DefaultFullHttpRequest对象包括协议、请求方法、uri、请求头、请求体数据（Get没有）
        FullHttpRequest newRequest = new DefaultFullHttpRequest(httpRequest.protocolVersion(), httpRequest.method(), httpRequest.uri(), requestData);
        newRequest.headers().add(httpRequest.headers());
        return newRequest;
    }
}
