package org.howard1209a.server.handler;

import io.netty.channel.*;
import org.howard1209a.configure.pojo.Address;
import org.howard1209a.server.Server;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.dispatcher.Dispatcher;
import org.howard1209a.server.pojo.HttpRequestWrapper;

public class DispatchHandler extends ChannelInboundHandlerAdapter {
    private Dispatcher dispatcher;

    public DispatchHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequestWrapper wrapper = (HttpRequestWrapper) msg;
        Address address = dispatcher.dispatch(wrapper);

        ChannelFuture connectFuture = Server.getInstance().connect(address.getIp(), Integer.parseInt(address.getPort()));
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel upStreamChannel = channelFuture.channel();
                StreamManager.getInstance().put(upStreamChannel, wrapper.getDownStreamChannel());
                upStreamChannel.writeAndFlush(wrapper.getRequest());
            }
        });
    }
}
