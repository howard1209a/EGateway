package org.howard1209a.server.dispatcher;

import org.howard1209a.configure.ServerConfiguration;
import org.howard1209a.configure.pojo.Address;
import org.howard1209a.configure.pojo.Gateway;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PollingDispatcher implements Dispatcher {
    private static final PollingDispatcher POLLING_DISPATCHER = new PollingDispatcher();
    private ConcurrentHashMap<Route, AtomicInteger> pollingCache;

    private PollingDispatcher() {
        pollingCache = new ConcurrentHashMap<>();
        List<Route> routes = ServerConfiguration.getInfo().getRoutes();
        for (Route route : routes) {
            if (route.getLoadBalance().equals("polling")) {
                pollingCache.put(route, new AtomicInteger(0));
            }
        }
    }

    public static PollingDispatcher getInstance() {
        return POLLING_DISPATCHER;
    }

    @Override
    public Address dispatch(HttpRequestWrapper wrapper) {
        Route route = wrapper.getRoute();
        AtomicInteger count = pollingCache.get(route);
        List<Address> addresses = route.getAddresses();
        int weightSum = 0;
        for (Address address : addresses) {
            weightSum += address.getWeight();
        }
        int countNum = count.incrementAndGet() % weightSum;
        for (int i = 0; i < addresses.size(); i++) { // 权重轮询
            Address address = addresses.get(i);
            if (countNum < address.getWeight()) {
                return address;
            }
            countNum -= address.getWeight();
        }
        return null;
    }

    public static int findGCD(int a, int b) { // 求两个数的最大公因子
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
