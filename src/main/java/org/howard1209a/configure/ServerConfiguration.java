package org.howard1209a.configure;

import org.howard1209a.configure.pojo.Gateway;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

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
        Gateway confInfo = yaml.load(inputStream);
    }

    public static ServerConfiguration getServerConfiguration() {
        return conf;
    }

    public static void init() {}
}
