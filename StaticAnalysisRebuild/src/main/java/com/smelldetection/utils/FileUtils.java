package com.smelldetection.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
     * @param directory 本地项目路径
     * @return 返回 directory 下的所有配置文件路径列表
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
     * @param stack 存储 key 的栈
     * @param map 用于存储配置文件 key-value 的映射
     * @param yaml 读取的待解析的 yaml 文件内容
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
     * @param filePath 以properties结尾的配置文件路径
     * @param map 用于存储配置文件 key-value 的映射
     */
    public static void resolveProperties(String filePath, Map<String, String> map) throws IOException {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(filePath)));
        for (Object key : properties.keySet()) {
            map.put((String) key, properties.getProperty((String) key));
        }
    }

    /**
     * 获取当前路径中所有的 pom.xml 文件
     * @param directory 路径
     * @return pom.xml 文件的路径列表
     */
    public static List<String> getPomXml(String directory) throws IOException {
        Path parent = Paths.get(directory);
        int maxDepth = 10;
        Stream<Path> stream = Files.find(parent, maxDepth,
                (filepath, attributes) -> String.valueOf(filepath).contains("pom.xml"));
        List<String> pomXml = stream.sorted().map(String::valueOf).filter(filepath ->{
            return !String.valueOf(filepath).toLowerCase().contains(".mvn")
                    && !String.valueOf(filepath).toLowerCase().contains("gradle");
        }).collect(Collectors.toList());
        return  pomXml;
    }

    /**
     * 获取本机 directory 项目路径下的所有微服务模块的完整路径
     * @param directory 微服务系统路径
     * @return 微服务系统所有微服务模块的完整路径
     */
    public static List<String> getServices(String directory) throws IOException {
        File[] files = new File(directory).listFiles();
        List<String> services = new ArrayList<>();
        if (files == null) {
            return services;
        }
        for (File file : files) {
            if (file.isDirectory() && FileUtils.getApplicationYamlOrProperties(file.getAbsolutePath()).size() != 0) {
                services.add(file.getAbsolutePath());
            }
        }
        return services;
    }

    /**
     * 获取 directory 目录下所有的 .java 文件，排除 test 目录中的文件
     * @param directory 待检索目录
     * @return .java 文件路径的列表
     */
    public static List<String> getJavaFiles(String directory) throws IOException {
        Path parent = Paths.get(directory);
        List<String> javaFiles;
        int maxDepth = 15;
        Stream<Path> stream = Files.find(parent, maxDepth, (filePath, attributes) -> String.valueOf(filePath).endsWith(".java"));
        // 忽略 test 目录下的 .java 文件，但是该目录以外的类名可以包含 test 或者 Test
        javaFiles = stream.sorted().map(String::valueOf).filter(filepath ->
                        (!String.valueOf(filepath).contains("\\test\\")
                                && !String.valueOf(filepath).contains("/test/"))).collect(Collectors.toList());
        return javaFiles;
    }
}
