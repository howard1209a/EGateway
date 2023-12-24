package org.howard1209a.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.howard1209a.cache.basic.ByteMemory;
import org.howard1209a.cache.basic.DiskByteMemory;
import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Cache;
import org.howard1209a.configure.pojo.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

abstract public class CacheProvider<T> {
    private ConcurrentHashMap<Route, ByteMemory> memoryMap;
    private ObjectMapper objectMapper;

    protected CacheProvider() {
        this.memoryMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();

        List<Route> routes = ServerConfiguration.getInfo().getRoutes();
        for (Route route : routes) {
            Cache cache = route.getCache();
            this.memoryMap.put(route, new DiskByteMemory(cache));
        }
    }

    public void save(Route route, String key, String value) {
        ByteMemory byteMemory = memoryMap.get(route);
        byteMemory.set(key, value.getBytes());
    }

    public void save(String key, Object value) {
        try {
            String s = objectMapper.writeValueAsString(value);
            save(key, s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String load(Route route, String key) {
        ByteMemory byteMemory = memoryMap.get(route);
        byte[] bytes = byteMemory.get(key);
        return new String(bytes);
    }

    public <I> I load(Route route, String key, Class<I> c) {
        String value = load(route, key);
        try {
            return objectMapper.readValue(value, c);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void saveCache(String key, T value);

    public abstract T loadCache(String key);
}
