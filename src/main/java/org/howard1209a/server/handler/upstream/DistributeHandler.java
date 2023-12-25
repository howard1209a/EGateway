package org.howard1209a.server.handler.upstream;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Downstream;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.StreamManager;

import java.util.concurrent.TimeUnit;

public class DistributeHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;

        Channel upStreamChannel = ctx.channel();
        Channel downStreamChannel = StreamManager.getInstance().get(upStreamChannel);
        StreamManager.getInstance().remove(upStreamChannel); // 删除映射
        upStreamChannel.close(); // 异步关闭upStreamChannel

        KeepaliveManager keepaliveManager = KeepaliveManager.getInstance();
        if (keepaliveManager.contain(downStreamChannel)) { // 持久连接响应添加一些头部
            Downstream downstream = ServerConfiguration.getInfo().getProtocol().getDownstream();
            HttpHeaders headers = response.headers();
            headers.add("Connection", "keep-alive");
            headers.add("Keep-Alive", "timeout=" + downstream.getTimeout() + ", max=" + downstream.getMax());
        }
        ChannelFuture distributeFuture = downStreamChannel.writeAndFlush(response);

        keepaliveManager.handleDownStreamChannelClose(downStreamChannel, distributeFuture);
    }
}
