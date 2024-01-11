package org.howard1209a.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import java.util.concurrent.ConcurrentHashMap;

public class StreamManager extends ConcurrentHashMap<Channel, StreamManager.Loopback> { // upstream -> Loopback
    private static final StreamManager streamManager = new StreamManager();

    public static StreamManager getInstance() {
        return streamManager;
    }

    public Channel getDownStreamChannel(Channel upStreamChannel) {
        Loopback loopback = get(upStreamChannel);
        return loopback.getDownStreamChannel();
    }

    public FullHttpRequest getFullHttpRequest(Channel upStreamChannel) {
        Loopback loopback = get(upStreamChannel);
        return loopback.getRequest();
    }

    public boolean isNotCache(Channel upStreamChannel) {
        Loopback loopback = get(upStreamChannel);
        return loopback.isNotCache();
    }

    public Route getRoute(Channel upStreamChannel) {
        Loopback loopback = get(upStreamChannel);
        return loopback.getRoute();
    }

    public void putLoopback(Channel upStreamChannel, HttpRequestWrapper wrapper) {
        put(upStreamChannel, new Loopback(wrapper.getDownStreamChannel(), wrapper.getRequest(), wrapper.getRoute(), wrapper.isNotCache()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Loopback {
        private Channel downStreamChannel;
        private FullHttpRequest request;
        private Route route;
        private boolean notCache;
    }
}
