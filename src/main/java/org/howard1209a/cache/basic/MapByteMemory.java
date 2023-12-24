package org.howard1209a.cache.basic;

import java.util.concurrent.ConcurrentHashMap;

public class MapByteMemory implements ByteMemory {
    private ConcurrentHashMap<String, byte[]> data;

    public MapByteMemory() {
        this.data = new ConcurrentHashMap<>();
    }

    @Override
    public void set(String key, byte[] value) {
        data.put(key, value);
    }

    @Override
    public byte[] get(String key) {
        return data.get(key);
    }
}
