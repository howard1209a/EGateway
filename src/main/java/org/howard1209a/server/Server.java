package org.howard1209a.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.exception.ServerRepeatStartException;
import org.howard1209a.server.handler.RouteHandler;

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
        initBootstrap();
        initServerBootstrap();

    }

    private void initBootstrap() {
        NioEventLoopGroup clientWorker = new NioEventLoopGroup(2);
        this.bootstrap = new Bootstrap();
        this.bootstrap.channel(NioSocketChannel.class)
                .group(clientWorker)
                .handler(new ChannelInboundHandlerAdapter());
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
                        nioSocketChannel.pipeline().addLast(new HttpServerCodec());
                        nioSocketChannel.pipeline().addLast(new RouteHandler());
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
        try {
            Server.run();
        } catch (ServerRepeatStartException e) {
            throw new RuntimeException(e);
        }
    }
}
