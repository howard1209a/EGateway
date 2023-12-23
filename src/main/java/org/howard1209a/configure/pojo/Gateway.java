package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gateway {
    private Protocal protocol;
    private List<Route> routes;

    public Route matchRoute(String uri) {
        for (Route route : routes) {
            if (route.matches(uri)) {
                return route;
            }
        }
        return null;
    }
}
