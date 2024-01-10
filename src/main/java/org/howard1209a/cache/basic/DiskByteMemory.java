package org.howard1209a.cache.basic;

import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Cache;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.exception.RootPathNotEmptyException;
import org.howard1209a.exception.RootPathNotExistException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class DiskByteMemory implements ByteMemory {
    private String rootPath;
    private List<String> chunkPaths;
    private AtomicInteger chunkIndex;
    private ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap;
    private ConcurrentHashMap<String, String> metaDataMap;


    public DiskByteMemory(Cache cache) {
        this.rootPath = cache.getPath();
        this.chunkPaths = new Vector<>();
        this.chunkIndex = new AtomicInteger(0);
        this.lockMap = new ConcurrentHashMap<>();
        if (cache.isMetadata()) {
            this.metaDataMap = new ConcurrentHashMap<>();
        }
        generateCacheStructure(cache);
    }

    private void generateCacheStructure(Cache cache) { // 创建缓存目录结构
        String[] split = cache.getLevel().split("/");
        int[] level = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            level[i] = Integer.parseInt(split[i]);
        }

        File directory = new File(this.rootPath);
        if (!directory.exists()) {
            throw new RootPathNotExistException("root path do not exist!");
        }
        if (directory.listFiles().length != 0) {
            throw new RootPathNotEmptyException("root path do not empty!");
        }

        backtracking(level, 0, new StringBuilder(this.rootPath));
    }

    private void backtracking(int[] level, int depth, StringBuilder sb) { // 回溯构建cache目录
        int num = pow(16, level[depth]);
        for (int i = 0; i < num; i++) {
            String directoryName = Integer.toHexString(i);
            sb.append("/" + directoryName);
            File directory = new File(sb.toString());
            directory.mkdir();

            if (depth == level.length - 1) {
                chunkPaths.add(sb.toString());
            }

            if (depth < level.length - 1) {
                backtracking(level, depth + 1, sb);
            }
            sb.delete(sb.lastIndexOf("/"), sb.length());
        }
    }

    private int pow(int a, int b) {
        int num = 1;
        for (int i = 1; i <= b; i++) {
            num *= a;
        }
        return num;
    }

    @Override
    public void set(String key, byte[] value) { // 存储字节数据到磁盘单个文件
        // lazy generate lock
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(key, new Function<String, ReentrantReadWriteLock>() {
            @Override
            public ReentrantReadWriteLock apply(String s) {
                return new ReentrantReadWriteLock();
            }
        });
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        FileOutputStream fileOutputStream = null;
        writeLock.lock();
        try {
            File file = findFile(new File(this.rootPath), key);
            String chunkPath = (file == null ? chunkPaths.get(chunkIndex.getAndIncrement() % chunkPaths.size()) + "/" + key : file.getAbsolutePath());
            fileOutputStream = new FileOutputStream(chunkPath, false);
            fileOutputStream.write(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public byte[] get(String key) { // 获取磁盘单个文件存储的字节数据
        // lazy generate lock
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(key, new Function<String, ReentrantReadWriteLock>() {
            @Override
            public ReentrantReadWriteLock apply(String s) {
                return new ReentrantReadWriteLock();
            }
        });
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        FileInputStream fileInputStream = null;
        readLock.lock();
        try {
            File file = findFile(new File(this.rootPath), key);
            if (file == null) { // 不存在文件，返回null
                return null;
            }
            String chunkPath = file.getAbsolutePath();
            fileInputStream = new FileInputStream(chunkPath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }
    }

    @Override
    public void delete(String key) { // 删除磁盘单个文件
        // lazy generate lock
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(key, new Function<String, ReentrantReadWriteLock>() {
            @Override
            public ReentrantReadWriteLock apply(String s) {
                return new ReentrantReadWriteLock();
            }
        });

        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            File file = findFile(new File(this.rootPath), key);
            if (file == null) { // 不存在文件，返回
                return;
            }
            file.delete();
        } finally {
            writeLock.unlock();
        }
    }

    private File findFile(File inputFile, String key) { // 递归寻找inputFile目录下的key文件
        File[] files = inputFile.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().equals(key)) {
                    return file;
                }
            } else {
                File res = findFile(file, key);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }
}
