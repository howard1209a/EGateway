package org.howard1209a.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.exception.ServerRepeatStartException;
import org.howard1209a.server.handler.downstream.*;
import org.howard1209a.server.handler.upstream.CacheSaveHandler;
import org.howard1209a.server.handler.upstream.DistributeHandler;
import org.howard1209a.server.handler.upstream.FullHttpResponseAggregator;
import org.howard1209a.server.handler.upstream.HeaderAddHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class Server { // single
    private static Server server;
    private Bootstrap bootstrap;
    private ServerBootstrap serverBootstrap;
    private ScheduledExecutorService scheduledExecutorService;

    private Server() {
        ServerConfiguration.init();
        ResponseCacheProvider.init();
        initBootstrap();
        initServerBootstrap();
        initMonitorThread();
    }

    private void initBootstrap() {
        NioEventLoopGroup clientWorker = new NioEventLoopGroup(2);
        this.bootstrap = new Bootstrap();
        this.bootstrap.channel(NioSocketChannel.class)
                .group(clientWorker)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpClientCodec());
                        socketChannel.pipeline().addLast(new FullHttpResponseAggregator());
                        socketChannel.pipeline().addLast(new CacheSaveHandler());
                        socketChannel.pipeline().addLast(new HeaderAddHandler());
                        socketChannel.pipeline().addLast(new DistributeHandler());
                    }
                });
    }

    private void initServerBootstrap() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup serverWorker = new NioEventLoopGroup(2);
        this.serverBootstrap = new ServerBootstrap();

        this.serverBootstrap.group(boss, serverWorker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                        nioSocketChannel.pipeline().addLast("FullHttpRequestAggregator", new FullHttpRequestAggregator());
                        nioSocketChannel.pipeline().addLast("RouteHandler", new RouteHandler());
                        nioSocketChannel.pipeline().addLast("HeaderParseHandler", new HeaderParseHandler());
                        nioSocketChannel.pipeline().addLast("CacheLoadHandler", new CacheLoadHandler());
                        nioSocketChannel.pipeline().addLast("DispatchHandler", new DispatchHandler());
                    }
                })
                .bind(12090);
    }

    private void initMonitorThread() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public ChannelFuture connect(String inetHost, int inetPort) {
        return this.bootstrap.connect(inetHost, inetPort);
    }


    public static void run() {
        synchronized (Server.class) {
            if (server == null) {
                server = new Server();
            } else {
                throw new ServerRepeatStartException("server is already started");
            }
        }
    }

    public static Server getInstance() {
        return server;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void main(String[] args) {
        Server.run();
    }
}
