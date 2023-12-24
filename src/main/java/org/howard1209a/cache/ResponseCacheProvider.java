package org.howard1209a.cache;

import io.netty.handler.codec.http.FullHttpResponse;
import org.howard1209a.cache.basic.ByteMemory;

public class ResponseCacheProvider extends CacheProvider<FullHttpResponse> {
    private static final ResponseCacheProvider RESPONSE_CACHE_PROVIDER = new ResponseCacheProvider();

    public static ResponseCacheProvider getInstance() {
        return RESPONSE_CACHE_PROVIDER;
    }

    @Override
    public void saveCache(String key, FullHttpResponse value) {

    }

    @Override
    public FullHttpResponse loadCache(String key) {
        return null;
    }

    public static void init() {}
}
