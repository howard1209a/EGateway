import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import org.howard1209a.exception.ServerException;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.Map;
import java.util.Random;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

public class T1 {

    @Data
    public static class A {
        public String param;
    }

    @Test
    public void t1() throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(Wrapper.class));
        InputStream inputStream = new FileInputStream(new File("/Users/howard1209a/Desktop/codes/EGateway/h.yaml"));
        Wrapper wrapper = yaml.load(inputStream);
        System.out.println(wrapper.test.param);
    }

    @Test
    public void loadToMap() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = T1.class
                .getClassLoader()
                .getResourceAsStream("/Users/howard1209a/Desktop/codes/EGateway/h.yaml");
        int read = inputStream.read();
    }

    @Test
    public void testClient() throws InterruptedException {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(worker);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
            }
        });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 12091).sync();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeBytes("11".getBytes());
        channelFuture.channel().writeAndFlush(buffer);
        channelFuture.channel().closeFuture().sync();
    }

    @Test
    public void t5() throws ServerException {
        throw new ServerException();
    }

    @Test
    public void t6() throws InterruptedException {
        ChannelFuture future = new ServerBootstrap().group(new NioEventLoopGroup(2)).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new HttpServerCodec());
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                        response.headers().setInt(CONTENT_LENGTH, 0);
                        ctx.writeAndFlush(response);
                    }
                });
            }
        }).bind(12091);
        future.sync();
        future.channel().closeFuture().sync();
    }
}
