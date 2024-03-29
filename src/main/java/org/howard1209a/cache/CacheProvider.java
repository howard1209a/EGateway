package org.howard1209a.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.howard1209a.cache.basic.ByteMemory;
import org.howard1209a.cache.basic.DiskByteMemory;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Cache;
import org.howard1209a.configure.pojo.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CacheProvider<T> {
    private ConcurrentHashMap<Route, ByteMemory> memoryMap;
    private ObjectMapper objectMapper;

    protected CacheProvider() {
        this.memoryMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();

        List<Route> routes = ServerConfiguration.getInfo().getRoutes();
        for (Route route : routes) {
            Cache cache = route.getCache();
            if (cache != null) {
                this.memoryMap.put(route, new DiskByteMemory(cache));
            }
        }
    }

    protected final void save(Route route, String key, String value) {
        ByteMemory byteMemory = memoryMap.get(route);
        byteMemory.set(key, value.getBytes());
    }

    protected final void save(Route route, String key, Object value) {
        try {
            String s = objectMapper.writeValueAsString(value);
            save(route, key, s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected final String load(Route route, String key) {
        ByteMemory byteMemory = memoryMap.get(route);
        byte[] bytes = byteMemory.get(key);
        return bytes != null ? new String(bytes) : null;
    }

    protected final <I> I load(Route route, String key, Class<I> c) {
        String value = load(route, key);
        try {
            return value != null ? objectMapper.readValue(value, c) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void delete(Route route, String key) {
        ByteMemory byteMemory = memoryMap.get(route);
        byteMemory.delete(key);
    }

    public abstract void saveCache(Route route, String key, T value);

    public abstract T loadCache(Route route, String key);

    public abstract void deleteCache(Route route, String key);
}
