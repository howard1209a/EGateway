package org.howard1209a.server.handler.downstream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.howard1209a.cache.CacheProvider;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.cache.pojo.PersistentResponse;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.KeepaliveManager;
import org.howard1209a.server.StreamManager;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class CacheLoadHandler extends ChannelInboundHandlerAdapter {
    private static Map<String, ReentrantLock> rebuildLockMap = new ConcurrentHashMap<>();

    public static void unlock(String key) {
        ReentrantLock reentrantLock = rebuildLockMap.get(key);
        reentrantLock.unlock();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TimedCacheRelease implements Runnable {
        private Route route;
        private String key;

        @Override
        public void run() { // 删除缓存
            CacheProvider<PersistentResponse> cacheProvider = ResponseCacheProvider.getInstance();
            cacheProvider.deleteCache(route, key);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpRequestWrapper wrapper = (HttpRequestWrapper) msg;
        CacheProvider<PersistentResponse> cacheProvider = ResponseCacheProvider.getInstance();
        String key = ResponseCacheProvider.FullHttpRequest2MD5(wrapper.getRequest());
        PersistentResponse persistentResponse = cacheProvider.loadCache(wrapper.getRoute(), key);

        if (persistentResponse == null) { // 缓存未命中
            // 防止缓存击穿，保证唯一缓存重建
            ReentrantLock reentrantLock = rebuildLockMap.computeIfAbsent(key, new Function<String, ReentrantLock>() {
                @Override
                public ReentrantLock apply(String s) {
                    return new ReentrantLock();
                }
            });

            // todo 这里有问题
            reentrantLock.lock();
            persistentResponse = cacheProvider.loadCache(wrapper.getRoute(), key);
            if (persistentResponse == null) { // double check
                // 设置一个定时任务释放缓存
                Integer expireTime = wrapper.getRoute().getCache().getExpireTime();
                ctx.channel().eventLoop().schedule(new TimedCacheRelease(wrapper.getRoute(), key), expireTime, TimeUnit.SECONDS);
                super.channelRead(ctx, msg);
                return;
            } else {
                reentrantLock.unlock();
            }
        }

        // 缓存命中
        DefaultFullHttpResponse response = ResponseCacheProvider.PersistentResponse2DefaultFullHttpResponse(persistentResponse);
        Channel downStreamChannel = ctx.channel();
        ChannelFuture channelFuture = downStreamChannel.writeAndFlush(response);
        KeepaliveManager.getInstance().handleDownStreamChannelClose(downStreamChannel, channelFuture);
    }
}
