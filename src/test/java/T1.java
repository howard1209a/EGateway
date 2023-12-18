import lombok.Data;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.Map;

public class T1 {

    @Data
    public static class A {
        public String param;
    }

    @Test
    public void t1() throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(Wrapper.class));
        InputStream inputStream = new FileInputStream(new File("/Users/howard1209a/Desktop/codes/EGateway/h.yaml"));
        Wrapper wrapper = yaml.load(inputStream);
        System.out.println(wrapper.test.param);
    }

    @Test
    public void loadToMap() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = T1.class
                .getClassLoader()
                .getResourceAsStream("/Users/howard1209a/Desktop/codes/EGateway/h.yaml");
        int read = inputStream.read();
    }
}
