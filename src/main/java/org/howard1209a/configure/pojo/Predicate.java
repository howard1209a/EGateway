package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Predicate {
    protected String path;
    public boolean matches(String uri) {
        throw new RuntimeException("method is not implemented");
    }

    public void processPattern() {
        throw new RuntimeException("method is not implemented");
    }
}
