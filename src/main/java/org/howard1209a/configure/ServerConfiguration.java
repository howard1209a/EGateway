package org.howard1209a.configure;

import org.howard1209a.configure.pojo.Gateway;
import org.howard1209a.configure.pojo.Predicate;
import org.howard1209a.configure.pojo.PreparedPredicate;
import org.howard1209a.configure.pojo.Route;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ServerConfiguration {
    private static final ServerConfiguration conf = new ServerConfiguration();

    private Gateway confInfo;

    private ServerConfiguration() {
        Yaml yaml = new Yaml(new Constructor(Gateway.class));
        String path = ServerConfiguration.class.getClassLoader().getResource("conf.yaml").getPath();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.confInfo = yaml.load(inputStream);

        processPattern();
        System.out.println("1");
    }

    public static Gateway getInfo() {
        return conf.confInfo;
    }

    public static void init() {
    }

    private void processPattern() {
        for (Route route : confInfo.getRoutes()) {
            List<Predicate> predicates = route.getPredicates();
            for (int i = 0; i < predicates.size(); i++) {
                predicates.set(i, new PreparedPredicate(predicates.get(i)));
            }
        }
    }
}
