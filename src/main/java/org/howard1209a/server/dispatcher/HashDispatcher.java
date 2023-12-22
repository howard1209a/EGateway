package org.howard1209a.server.dispatcher;

import io.netty.channel.Channel;
import org.howard1209a.configure.pojo.Address;
import org.howard1209a.configure.pojo.Route;
import org.howard1209a.server.pojo.HttpRequestWrapper;

import java.util.List;

public class HashDispatcher implements Dispatcher {
    @Override
    public Address dispatch(HttpRequestWrapper wrapper) {
        Channel downStreamChannel = wrapper.getDownStreamChannel();
        String s = downStreamChannel.remoteAddress().toString().split(":")[0];
        String sourceIp = s.substring(1, s.length());
        List<Address> addresses = wrapper.getRoute().getAddresses();
        Address address = addresses.get(sourceIp.hashCode() % addresses.size());
        return address;
    }
}
