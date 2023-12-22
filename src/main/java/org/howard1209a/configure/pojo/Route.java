package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Route {
    private String id;
    private List<Address> addresses;
    private List<Predicate> predicates;

    public boolean matches(String uri) {
        for (Predicate predicate : predicates) {
            if (predicate.matches(uri)) {
                return true;
            }
        }
        return false;
    }
}
