package org.howard1209a.server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Downstream;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.StreamManager;

import java.util.concurrent.TimeUnit;

public class DistributeHandler extends SimpleChannelInboundHandler<HttpObject> {
    private HttpResponse httpResponse;
    private StringBuilder responseData = new StringBuilder();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            // 接收本次响应头部信息
            httpResponse = (HttpResponse) msg;
        } else if (msg instanceof HttpContent) {
            // 接收本次响应体数据
            HttpContent httpContent = (HttpContent) msg;
            responseData.append(httpContent.content().toString(CharsetUtil.UTF_8));

            // 如果是 LastHttpContent，表示响应结束
            if (msg instanceof LastHttpContent) {
                Channel upStreamChannel = channelHandlerContext.channel();
                Channel downStreamChannel = StreamManager.getInstance().get(upStreamChannel);
                StreamManager.getInstance().remove(upStreamChannel);
                upStreamChannel.close();

                String fullResponseData = responseData.toString();
                FullHttpResponse response = generateResponse(httpResponse, fullResponseData);

                KeepaliveManager keepaliveManager = KeepaliveManager.getInstance();
                KeepaliveManager.KeepaliveState keepaliveState = keepaliveManager.get(downStreamChannel);
                if (keepaliveState != null) {
                    Downstream downstream = ServerConfiguration.getInfo().getProtocol().getDownstream();
                    HttpHeaders headers = response.headers();
                    headers.add("Connection", "keep-alive");
                    headers.add("Keep-Alive", "timeout=" + downstream.getTimeout() + ", max=" + downstream.getMax());
                }
                ChannelFuture distributeFuture = downStreamChannel.writeAndFlush(response);
                if (keepaliveState != null) {
                    if (keepaliveState.getCurrentNum() == 0) {
                        Integer timeout = ServerConfiguration.getInfo().getProtocol().getDownstream().getTimeout();
                        downStreamChannel.eventLoop().schedule(new KeepaliveManager.TimeoutCheck(downStreamChannel), timeout, TimeUnit.SECONDS);
                    }
                    if (keepaliveState.isArriveMax()) {
                        keepaliveManager.remove(downStreamChannel);
                        closeDownStreamChannel(distributeFuture);
                    } else {
                        keepaliveState.increment();
                    }
                } else {
                    closeDownStreamChannel(distributeFuture);
                }

                // 清空 responseData 以便接收下一个响应
                responseData.setLength(0);
                httpResponse = null;
            }
        }
    }

    private FullHttpResponse generateResponse(HttpResponse response, String responseData) {
        FullHttpResponse newResponse = new DefaultFullHttpResponse(
                response.protocolVersion(),
                response.status(),
                Unpooled.copiedBuffer(responseData, CharsetUtil.UTF_8)
        );

        // 将原始响应头部信息拷贝到新的响应体中
        newResponse.headers().add(response.headers());

        return newResponse;
    }

    private void closeDownStreamChannel(ChannelFuture distributeFuture) {
        distributeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.channel().close();
            }
        });
    }
}
