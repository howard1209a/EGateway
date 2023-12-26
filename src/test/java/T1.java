import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import org.howard1209a.cache.ResponseCacheProvider;
import org.howard1209a.cache.pojo.PersistentResponse;
import org.howard1209a.exception.ServerException;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private static int count = 1;

    @Test
    public void testClient() throws InterruptedException {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(worker);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ctx.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/static"));
                        System.out.println(count++);
                    }
                });
            }
        });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 12090).sync();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeBytes("h".getBytes());
        channelFuture.channel().writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/static", buffer));
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
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                        byte[] bytes = "<h1>Hello, world!</h1>".getBytes();
                        response.headers().setInt(CONTENT_LENGTH, bytes.length);
                        response.content().writeBytes(bytes);
                        ctx.writeAndFlush(response);
                    }
                });
            }
        }).bind(12091);
        future.sync();
        future.channel().closeFuture().sync();
    }

    @Test
    public void t7() throws InterruptedException {
        ChannelFuture future = new ServerBootstrap().group(new NioEventLoopGroup(2)).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new HttpServerCodec());
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                        byte[] bytes = "<h1>Hello, world!</h1>".getBytes();
                        response.headers().setInt(CONTENT_LENGTH, bytes.length);
                        response.content().writeBytes(bytes);
                        ctx.writeAndFlush(response);
                    }
                });
            }
        }).bind(12092);
        future.sync();
        future.channel().closeFuture().sync();
    }

    @Test
    public void t8() {
        int num1 = 1;
        int num2 = 1;

        // 计算最大公因子
        int gcd = findGCD(num1, num2);

        // 消去公因子后的结果
        int result1 = num1 / gcd;
        int result2 = num2 / gcd;

        System.out.println("原始数值: " + num1 + " 和 " + num2);
        System.out.println("最大公因子: " + gcd);
        System.out.println("消去公因子后的结果: " + result1 + " 和 " + result2);
    }

    public static int findGCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    @Test
    public void t9() throws IOException {
        ByteBuf byteBuf=ByteBufAllocator.DEFAULT.heapBuffer();
        ByteBuf byteBuf1=ByteBufAllocator.DEFAULT.heapBuffer();
        ByteBuf byteBuf2=ByteBufAllocator.DEFAULT.heapBuffer();
        byteBuf1.writeBytes("hello".getBytes());
        byteBuf2.writeBytes("world".getBytes());
        byteBuf.writeBytes(byteBuf1);
        byteBuf.writeBytes(byteBuf2);
        byte[] buffer=new byte[100];
        byteBuf.readBytes(buffer,0,10);
        System.out.println(new String(buffer));
    }

    @Test
    public void t10() {
        String input = "Hello, MD5!";

        try {
            // 获取MD5实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算MD5摘要
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为BigInteger
            BigInteger number = new BigInteger(1, messageDigest);

            // 将BigInteger转换为16进制字符串
            String md5 = number.toString(16);

            // 补齐前导零，确保输出为32位
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }

            System.out.println("MD5 Hash: " + md5);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void t11() throws JsonProcessingException {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        byteBuf.writeBytes("hello".getBytes());
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, byteBuf);
        response.headers().add("co","hi");
        PersistentResponse persistentResponse = ResponseCacheProvider.DefaultFullHttpResponse2PersistentResponse(response);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(persistentResponse);
        PersistentResponse result = objectMapper.readValue(json, PersistentResponse.class);
        DefaultFullHttpResponse response1 = ResponseCacheProvider.PersistentResponse2DefaultFullHttpResponse(result);
        System.out.println(response1);
    }
}
