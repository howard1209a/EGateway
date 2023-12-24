package org.howard1209a.cache.basic;

public interface ByteMemory { // ByteMemory类下的所有方法的线程安全性，都需要实现类自己去保证
    void set(String key, byte[] value);

    byte[] get(String key);
}
