package org.howard1209a.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ConcurrentHashMap;

public class StreamManager extends ConcurrentHashMap<Channel, Channel>{ // upstream -> downstream
    private static final StreamManager streamManager = new StreamManager();

    public static StreamManager getInstance() {
        return streamManager;
    }
}
