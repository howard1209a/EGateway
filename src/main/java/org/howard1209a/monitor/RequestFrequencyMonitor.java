package org.howard1209a.monitor;

import com.sun.org.apache.xerces.internal.impl.dv.xs.AnyURIDV;
import org.howard1209a.monitor.FrequencyMonitor;
import org.howard1209a.server.Server;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RequestFrequencyMonitor implements FrequencyMonitor {
    private static RequestFrequencyMonitor monitor;
    private Map<String, AtomicReference<List<AtomicInteger>>> fListMap;
    private Map<String, ReentrantReadWriteLock> protectedLockMap;

    private class TimedUpdate implements Runnable {
        private String key;

        public TimedUpdate(String key) {
            this.key = key;
        }

        @Override
        public void run() { // thread-safe
            ReentrantReadWriteLock.WriteLock writeLock = getProtectedLock(key).writeLock();
            writeLock.lock();
            try {
                List<AtomicInteger> fList = fListMap.get(key).get();
                fList.remove(0);
                fList.add(new AtomicInteger(0));
            } finally {
                writeLock.unlock();
            }
        }
    }

    public RequestFrequencyMonitor() {
        this.fListMap = new ConcurrentHashMap<>();
        this.protectedLockMap = new ConcurrentHashMap<>();
    }

    public static RequestFrequencyMonitor getInstance() { // lazy单例
        if (monitor != null) {
            return monitor;
        }
        synchronized (RequestFrequencyMonitor.class) {
            if (monitor == null) {
                monitor = new RequestFrequencyMonitor();
            }
            return monitor;
        }
    }

    @Override
    public Integer getFrequencyPerSecond(String key) { // thread-safe
        ReentrantReadWriteLock.ReadLock readLock = getProtectedLock(key).readLock();
        readLock.lock();
        try {
            List<AtomicInteger> fList = fListMap.get(key).get();
            int frequencyPerSecond = 0;
            for (int i = 0; i < 10; i++) {
                frequencyPerSecond += fList.get(i).intValue();
            }
            System.out.println(frequencyPerSecond);
            return frequencyPerSecond;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void record(String key) { // thread-safe
        ReentrantReadWriteLock.ReadLock readLock = getProtectedLock(key).readLock();
        readLock.lock();
        try {
            List<AtomicInteger> fList = fListMap.get(key).get();
            AtomicInteger count = fList.get(fList.size() - 1);
            count.incrementAndGet();
        } finally {
            readLock.unlock();
        }
    }

    public void startMonitor(String key) { // thread-safe
        int rate = 100;
        AtomicReference<List<AtomicInteger>> listAtomicReference = fListMap.computeIfAbsent(key, new Function<String, AtomicReference<List<AtomicInteger>>>() {
            @Override
            public AtomicReference<List<AtomicInteger>> apply(String s) {
                return new AtomicReference<>();
            }
        });
        List<AtomicInteger> fList = new Vector<>();
        for (int i = 0; i < 11; i++) {
            fList.add(new AtomicInteger(0));
        }
        boolean success = listAtomicReference.compareAndSet(null, fList);

        if (success) {
            ScheduledExecutorService scheduledExecutorService = Server.getInstance().getScheduledExecutorService();
            scheduledExecutorService.scheduleAtFixedRate(new TimedUpdate(key), rate, rate, TimeUnit.MILLISECONDS);
        }
    }

    private ReentrantReadWriteLock getProtectedLock(String key) { // thread-safe
        ReentrantReadWriteLock reentrantReadWriteLock = protectedLockMap.computeIfAbsent(key, new Function<String, ReentrantReadWriteLock>() {
            @Override
            public ReentrantReadWriteLock apply(String s) {
                return new ReentrantReadWriteLock();
            }
        });
        return reentrantReadWriteLock;
    }
}
