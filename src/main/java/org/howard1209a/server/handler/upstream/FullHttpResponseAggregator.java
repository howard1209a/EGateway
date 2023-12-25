package org.howard1209a.server.handler.upstream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class FullHttpResponseAggregator extends ChannelInboundHandlerAdapter {
    private HttpResponse httpResponse;
    private ByteBuf responseData;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { // 聚合出完整的DefaultFullHttpResponse
        if (msg instanceof HttpResponse) { // 响应行和响应头信息
            httpResponse = (HttpResponse) msg;
            responseData = ByteBufAllocator.DEFAULT.heapBuffer();
        } else if (msg instanceof HttpContent) { // 响应体信息
            HttpContent httpContent = (HttpContent) msg;
            responseData.writeBytes(httpContent.content());
            if (msg instanceof LastHttpContent) {
                FullHttpResponse response = generateResponse();
                super.channelRead(ctx, response);
            }
        }
    }

    private FullHttpResponse generateResponse() { // DefaultFullHttpResponse对象包括协议、状态码、响应头、响应体数据
        FullHttpResponse newResponse = new DefaultFullHttpResponse(httpResponse.protocolVersion(), httpResponse.status(), responseData);
        newResponse.headers().add(httpResponse.headers());
        return newResponse;
    }
}
