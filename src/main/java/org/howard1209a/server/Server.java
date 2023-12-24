package org.howard1209a.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.exception.ServerRepeatStartException;
import org.howard1209a.server.dispatcher.HashDispatcher;
import org.howard1209a.server.dispatcher.PollingDispatcher;
import org.howard1209a.server.handler.*;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class Server {
    private static Server server;
    private Bootstrap bootstrap;
    private ServerBootstrap serverBootstrap;

    private Server() {
        ServerConfiguration.init();
        ResponseCacheProvider.init();
        initBootstrap();
        initServerBootstrap();
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
                        nioSocketChannel.pipeline().addLast("RouteHandler", new RouteHandler());
                        nioSocketChannel.pipeline().addLast("HeaderHandler", new HeaderHandler());
                        nioSocketChannel.pipeline().addLast("DispatchHandler", new DispatchHandler());
                    }
                })
                .bind(12090);
    }

    public ChannelFuture connect(String inetHost, int inetPort) {
        return this.bootstrap.connect(inetHost, inetPort);
    }


    public static void run() throws ServerRepeatStartException {
        if (server == null) {
            server = new Server();
        } else {
            throw new ServerRepeatStartException("server is already started");
        }
    }

    public static Server getInstance() {
        return server;
    }

    public static void main(String[] args) {
        Server.run();
    }
}
