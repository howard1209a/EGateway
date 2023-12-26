package org.howard1209a.cache.pojo;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistentResponse {
    private String protocolVersion;
    private int status;
    private byte[] data;
    private List<Header> headers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Header {
        private String name;
        private String value;
    }
}
