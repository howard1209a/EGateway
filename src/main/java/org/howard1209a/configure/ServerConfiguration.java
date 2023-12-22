package org.howard1209a.configure;

import org.howard1209a.configure.pojo.*;
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
        processAddressWeight();
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

    private void processAddressWeight() {
        for (Route route : confInfo.getRoutes()) {
            List<Address> addresses = route.getAddresses();
            int[] weightArr = new int[addresses.size()];
            for (int i = 0; i < addresses.size(); i++) {
                weightArr[i] = addresses.get(i).getWeight();
            }
            int multiGCD = findMultiGCD(weightArr);
            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                address.setWeight(address.getWeight() / multiGCD);
            }
        }
    }

    // 使用欧几里得算法计算两个数的最大公因子
    private static int findGCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // 使用欧几里得算法计算任意多个数的最大公因子
    public static int findMultiGCD(int[] numbers) {
        if (numbers.length < 2) {
            return numbers[0];
        }

        int gcd = findGCD(numbers[0], numbers[1]);

        for (int i = 2; i < numbers.length; i++) {
            gcd = findGCD(gcd, numbers[i]);
        }

        return gcd;
    }
}
