package org.howard1209a.cache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.*;
import org.howard1209a.cache.basic.ByteMemory;
import org.howard1209a.cache.pojo.PersistentResponse;
import org.howard1209a.configure.pojo.Route;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseCacheProvider extends CacheProvider<PersistentResponse> {
    private static final ResponseCacheProvider RESPONSE_CACHE_PROVIDER = new ResponseCacheProvider();

    public static CacheProvider<PersistentResponse> getInstance() {
        return RESPONSE_CACHE_PROVIDER;
    }

    private ResponseCacheProvider() {
    }

    @Override
    public void saveCache(Route route, String key, PersistentResponse value) {
        super.save(route, key, value);
    }

    @Override
    public PersistentResponse loadCache(Route route, String key) {
        PersistentResponse response = super.load(route, key, PersistentResponse.class);
        return response;
    }

    @Override
    public void deleteCache(Route route, String key) {
        super.delete(route, key);
    }

    public static void init() {
    }


    public static String FullHttpRequest2MD5(FullHttpRequest request) {
        ByteBuf payload = request.content();
        payload.resetReaderIndex();
        String input = request.method() + request.uri() + payload.toString(java.nio.charset.StandardCharsets.UTF_8);
        return MD5(input);
    }

    private static String MD5(String input) {
        String md5 = null;
        try {
            // 获取MD5实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算MD5摘要
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为BigInteger
            BigInteger number = new BigInteger(1, messageDigest);

            // 将BigInteger转换为16进制字符串
            md5 = number.toString(16);

            // 补齐前导零，确保输出为32位
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    // DefaultFullHttpResponse转可序列化的pojo
    public static PersistentResponse DefaultFullHttpResponse2PersistentResponse(FullHttpResponse fullHttpResponse) {
        DefaultFullHttpResponse response = (DefaultFullHttpResponse) fullHttpResponse;

        String protocolVersion = response.protocolVersion().toString();
        int status = response.status().code();
        ByteBuf payload = response.content();
        byte[] data = new byte[payload.readableBytes()];
        payload.readBytes(data);
        List<PersistentResponse.Header> headers = new ArrayList<>();
        for (Map.Entry<String, String> entry : response.headers().entries()) {
            headers.add(new PersistentResponse.Header(entry.getKey(), entry.getValue()));
        }

        return new PersistentResponse(protocolVersion, status, data, headers);
    }

    // 可序列化的pojo转DefaultFullHttpResponse
    public static DefaultFullHttpResponse PersistentResponse2DefaultFullHttpResponse(PersistentResponse persistentResponse) {
        HttpVersion httpVersion = HttpVersion.valueOf(persistentResponse.getProtocolVersion());
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(persistentResponse.getStatus());
        ByteBuf payload = ByteBufAllocator.DEFAULT.heapBuffer();
        payload.writeBytes(persistentResponse.getData());
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, httpResponseStatus, payload);

        HttpHeaders headers = response.headers();
        for (PersistentResponse.Header header : persistentResponse.getHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        return response;
    }
}
