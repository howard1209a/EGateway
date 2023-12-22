package org.howard1209a.server.pojo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.howard1209a.configure.pojo.Route;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpRequestWrapper {
    private HttpRequest request;
    private Route route;
    private Channel downStreamChannel;
}
