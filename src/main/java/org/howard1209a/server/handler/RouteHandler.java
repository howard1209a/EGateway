package org.howard1209a.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Gateway;

@Slf4j
public class RouteHandler extends SimpleChannelInboundHandler<HttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
        log.warn(httpRequest.uri());
        Gateway info = ServerConfiguration.getInfo();

    }
}
