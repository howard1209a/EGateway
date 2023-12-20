import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        // 发送内容随机的数据包
                        Random r = new Random();
                        char c = 'a';
                        ByteBuf buffer = ctx.alloc().buffer();
                        for (int i = 0; i < 10; i++) {
                            byte[] bytes = new byte[8];
                            for (int j = 0; j < r.nextInt(8); j++) {
                                bytes[j] = (byte) c;
                            }
                            c++;
                            buffer.writeBytes(bytes);
                        }
                        ctx.writeAndFlush(buffer);
                    }
                });
            }
        });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 12090).sync();
        channelFuture.channel().closeFuture().sync();
    }

    @Test
    public void t5() throws ServerException {
        throw new ServerException();
    }
}
