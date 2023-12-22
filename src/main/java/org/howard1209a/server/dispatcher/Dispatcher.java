package org.howard1209a.server.dispatcher;

import org.howard1209a.configure.pojo.Address;
import org.howard1209a.server.pojo.HttpRequestWrapper;

public interface Dispatcher {
    Address dispatch(HttpRequestWrapper wrapper);
}
