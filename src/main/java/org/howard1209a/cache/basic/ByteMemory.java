package org.howard1209a.cache.basic;

public interface ByteMemory { // ByteMemory类下的所有方法的线程安全性，都需要实现类自己去保证
    // 如果不存在这个条目，则新增条目，如果存在这个条目，则覆盖条目
    void set(String key, byte[] value);

    // 如果不存在这个条目则返回null，如果存在这个条目则返回这个条目
    byte[] get(String key);

    // 如果存在这个条目则删除这个条目，如果不存在这个条目则什么都不做
    void delete(String key);
}
