package org.howard1209a.cache;

import io.netty.handler.codec.http.FullHttpResponse;
import org.howard1209a.cache.basic.ByteMemory;
import org.howard1209a.configure.pojo.Route;

public class ResponseCacheProvider extends CacheProvider<FullHttpResponse> {
    private static final ResponseCacheProvider RESPONSE_CACHE_PROVIDER = new ResponseCacheProvider();

    public static ResponseCacheProvider getInstance() {
        return RESPONSE_CACHE_PROVIDER;
    }

    @Override
    public void saveCache(Route route, String key, FullHttpResponse value) {
        super.save(route, key, value);
    }

    @Override
    public FullHttpResponse loadCache(Route route, String key) {
        FullHttpResponse response = super.load(route, key, FullHttpResponse.class);
        return response;
    }

    public static void init() {
    }
}
