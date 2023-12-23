package org.howard1209a.server;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Downstream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class KeepaliveManager {
    private static final KeepaliveManager MANAGER = new KeepaliveManager();

    private ConcurrentHashMap<Channel, KeepaliveState> cache;

    private KeepaliveManager() {
        cache = new ConcurrentHashMap<>();
    }

    public static KeepaliveManager getInstance() {
        return MANAGER;
    }

    public void register(Channel channel) {
        Downstream downstream = ServerConfiguration.getInfo().getProtocol().getDownstream();
        KeepaliveState keepaliveState = new KeepaliveState(channel, downstream.getTimeout(), 0, downstream.getMax());
        cache.put(channel, keepaliveState);
    }

    public KeepaliveState get(Channel channel) {
        return cache.get(channel);
    }

    public void remove(Channel channel) {
        cache.remove(channel);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KeepaliveState {
        private Channel channel;
        private Integer timeout;
        private Integer currentNum;
        private Integer maxNum;

        public boolean isArriveMax() {
            return currentNum.compareTo(maxNum - 1) == 0;
        }

        public void increment() {
            currentNum++;
        }
    }

    public static class TimeoutCheck implements Runnable {
        private Channel checkedChannel;
        private int lastNum;

        public TimeoutCheck(Channel checkedChannel) {
            this.checkedChannel = checkedChannel;
            this.lastNum = 1;
        }

        @Override
        public void run() {
            KeepaliveState keepaliveState = MANAGER.get(checkedChannel);
            if (keepaliveState.getCurrentNum() == lastNum) {
                checkedChannel.flush();
                checkedChannel.close();
                return;
            }
            Integer timeout = ServerConfiguration.getInfo().getProtocol().getDownstream().getTimeout();
            lastNum = keepaliveState.currentNum;
            checkedChannel.eventLoop().schedule(this, timeout, TimeUnit.SECONDS);
        }
    }
}
