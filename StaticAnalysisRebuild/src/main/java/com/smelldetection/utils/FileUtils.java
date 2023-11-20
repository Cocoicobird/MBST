package com.smelldetection.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class FileUtils {

    /**
     * 获取某一目录下的所有配置文件路径
     * @param directory
     * @return
     */
    public static List<String> getApplicationYamlOrProperties(String directory) throws IOException {
        Path parent = Paths.get(directory);
        int maxDepth = 10;
        Stream<Path> stream = Files.find(parent, maxDepth, (filePath, attributes) -> true);
        List<String> applicationYamlOrProperties = stream.sorted().map(String::valueOf).filter(filePath -> {
            return (String.valueOf(filePath).toLowerCase().endsWith("application.yml")
                    || String.valueOf(filePath).toLowerCase().endsWith("application.yaml")
                    || String.valueOf(filePath).toLowerCase().endsWith("application.properties"))
                    && !String.valueOf(filePath).toLowerCase().contains("target");
        }).collect(Collectors.toList());
        return applicationYamlOrProperties;
    }

    /**
     * 解析 yaml 里的数据数据存入到 map 中
     * @param stack
     * @param map
     * @param yaml
     */
    public static void resolveYaml(Stack<String> stack, Map<String, String> map, Map<String, Object> yaml) {
        for (String key : yaml.keySet()) {
            Object value = yaml.get(key);
            stack.add(key);
            if (value instanceof Map) {
                resolveYaml(stack, map, (Map<String, Object>) value);
            } else {
                map.put(String.join(".", stack), value.toString());
                stack.pop();
            }
        }
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    /**
     * 解析以properties结尾的配置文件 filePath 存入 map
     * @param filePath
     * @param map
     * @throws IOException
     */
    public static void resolveProperties(String filePath, Map<String, String> map) throws IOException {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(filePath)));
        for (Object key : properties.keySet()) {
            map.put((String) key, properties.getProperty((String) key));
        }
    }
}
