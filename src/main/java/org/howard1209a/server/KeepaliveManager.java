package org.howard1209a.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Downstream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class KeepaliveManager { // 持久连接管理
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

    public boolean contain(Channel channel) {
        return cache.get(channel) != null;
    }

    public void handleDownStreamChannelClose(Channel downStreamChannel, ChannelFuture distributeFuture) {
        KeepaliveState keepaliveState = get(downStreamChannel);
        if (keepaliveState != null) { // 持久连接
            if (keepaliveState.getCurrentNum() == 0) { // 持久连接的第一次响应，开启一个TimeoutCheck任务
                Integer timeout = ServerConfiguration.getInfo().getProtocol().getDownstream().getTimeout();
                downStreamChannel.eventLoop().schedule(new KeepaliveManager.TimeoutCheck(downStreamChannel), timeout, TimeUnit.SECONDS);
            }
            if (keepaliveState.isArriveMax()) { // 到达持久连接最大请求次数，关闭持久连接
                remove(downStreamChannel);
                closeDownStreamChannel(distributeFuture);
            } else { // 次数+1
                keepaliveState.increment();
            }
        } else { // 非持久连接直接关闭
            closeDownStreamChannel(distributeFuture);
        }
    }

    private void closeDownStreamChannel(ChannelFuture distributeFuture) {
        distributeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.channel().close();
            }
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class KeepaliveState { // KeepaliveState对象只存在于单线程，所以不用考虑线程安全
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

    public static class TimeoutCheck implements Runnable { // 每隔一段时间check一次，如果请求次数没变则关闭持久连接
        private Channel checkedChannel;
        private int lastNum;

        public TimeoutCheck(Channel checkedChannel) {
            this.checkedChannel = checkedChannel;
            this.lastNum = 1;
        }

        @Override
        public void run() {
            KeepaliveState keepaliveState = MANAGER.get(checkedChannel);
            if (keepaliveState.getCurrentNum() == lastNum) { // 在一定的时间内没有更多的loop完成，则异步关闭channel
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
