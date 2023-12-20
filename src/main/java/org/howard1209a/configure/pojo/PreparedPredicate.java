package org.howard1209a.configure.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreparedPredicate extends Predicate {
    private Pattern regex;

    public PreparedPredicate(Predicate predicate) {
        this.path = predicate.getPath();
        processPattern();
    }

    @Override
    public boolean matches(String uri) {
        return regex.matcher(uri).matches();
    }

    @Override
    public void processPattern() {
        int length = path.length();
        String pattern = path;
        if (length >= 3 && "/**".equals(path.substring(length - 3, length))) {
            pattern = "^" + path.substring(0, length - 3) + ".*$";
        } else {
            pattern = "^" + pattern + "$";
        }
        regex = Pattern.compile(pattern);
    }
}
