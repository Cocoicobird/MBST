package com.smelldetection.controller;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@RestController
@RequestMapping("/metric")
public class MetricController {

    @GetMapping("/extra")
    public String extra(HttpServletRequest request) throws IOException, XmlPullParserException {
        String microserviceSystemPath = request.getParameter("path");
        List<String> microservicePaths = FileUtils.getServices(microserviceSystemPath);
        Map<String, String> filePathToMicroserviceName = new HashMap<>();
        // 针对每一个微服务
        for (String microservicePath : microservicePaths) {
            System.out.println(microservicePath);
            List<String> javaFiles = FileUtils.getJavaFiles(microservicePath); // main 下所有 .java 文件
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(microservicePath); // 配置文件
            List<String> pomXml = FileUtils.getPomXml(microservicePath); // 依赖文件
            // 解析微服务名称
            String microserviceName = "";
            System.out.println("JavaFiles: " + javaFiles);
            List<Configuration> configurations = new ArrayList<>();
            for (String applicationYamlOrProperty : applicationYamlOrProperties) {
                Configuration configuration = new Configuration();
                if (applicationYamlOrProperty.endsWith("yaml") || applicationYamlOrProperty.endsWith("yml")) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yml = yaml.load(new FileInputStream(applicationYamlOrProperty));
                    FileUtils.resolveYaml(new Stack<>(), configuration.getItems(), yml);
                } else {
                    FileUtils.resolveProperties(applicationYamlOrProperty, configuration.getItems());
                }
                microserviceName = configuration.getItems().getOrDefault("spring.application.name", "");
                configurations.add(configuration);
            }
            filePathToMicroserviceName.put(microservicePath, microserviceName);
            // 解析依赖文件
            List<Pom> poms = new ArrayList<>();
            for (String p : pomXml) {
                Pom pom = new Pom();
                MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
                Model model = mavenXpp3Reader.read(new FileInputStream(p));
                pom.setMavenModel(model);
                poms.add(pom);
            }
            Set<String> dependencies = new LinkedHashSet<>();
            for (Pom pom : poms) {
                for (Dependency dependency : pom.getMavenModel().getDependencies()) {
                    if (!dependency.getGroupId().startsWith("org.springframework.boot")) {
                        dependencies.add(dependency.getGroupId() + "." + dependency.getArtifactId());
                    }
                }
            }
            int linesOfCode = 0; // 总代码行
            int entitiesFieldCount = 0; // 实体类所有属性个数
            List<String> entityClasses = new ArrayList<>(); // 实体类集合
            List<String> controllerClasses = new ArrayList<>(); // 控制器类集合
            List<String> interfaces = new ArrayList<>(); // 接口
            List<String> serviceImplementationClasses = new ArrayList<>();
            List<String> abstractClasses = new ArrayList<>(); // 抽象类
            List<String> dtoClasses = JavaParserUtils.getDtoClasses(javaFiles); // 数据传输类 DTO
            Set<String> apis = new LinkedHashSet<>(); // api
            Set<String> apiVersions = JavaParserUtils.getApiVersions(javaFiles, apis); // api 版本数
            // <微服务名称:<Service对象:<方法名:次数>>>
            Map<String, Map<String, Map<String, Integer>>> serviceMethodCallResults = new HashMap<>();
            serviceMethodCallResults.put(microserviceName, new HashMap<>());
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                // 代码行
                linesOfCode += FileUtils.getJavaFileLinesOfCode(file);
                // 实体类
                if (JavaParserUtils.isEntityClass(file)) {
                    entityClasses.add(javaFile);
                    entitiesFieldCount += JavaParserUtils.getEntityClassFieldCount(file);
                }
                // 控制器类
                if (JavaParserUtils.isControllerClass(file)) {
                    controllerClasses.add(javaFile);
                    Map<String, Map<String, Integer>> serviceMethodCallOfController = JavaParserUtils.getServiceMethodCallOfController(compilationUnit);
                    // 统计 service 层方法调用次数和比例
                    // service 对象名称
                    for (String serviceObject : serviceMethodCallOfController.keySet()) {
                        // 当前微服务模块未统计该 service 对象的方法调用
                        if (!serviceMethodCallResults.get(microserviceName).containsKey(serviceObject)) {
                            serviceMethodCallResults.get(microserviceName).put(serviceObject, new HashMap<>());
                        }
                        for (String serviceMethod : serviceMethodCallOfController.get(serviceObject).keySet()) {
                            Integer count = serviceMethodCallOfController.get(serviceObject).get(serviceMethod);
                            count += serviceMethodCallResults.get(microserviceName).get(serviceObject).getOrDefault(serviceMethod, 0);
                            serviceMethodCallResults.get(microserviceName).get(serviceObject).put(serviceMethod, count);
                        }
                    }
                }
                // 抽象类或接口
                String abstractClassOrInterface = JavaParserUtils.isAbstractClassOrInterface(file);
                if ("interface".equals(abstractClassOrInterface)) {
                    interfaces.add(javaFile);
                } else if ("abstract".equals(abstractClassOrInterface)) {
                    abstractClasses.add(javaFile);
                }
                // 服务实现类
                if ("ServiceImpl".equals(JavaParserUtils.isServiceImplementationClass(file))) {
                    serviceImplementationClasses.add(javaFile);
                }
            }
            // Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName); // 微服务间调用
            System.out.println("当前微服务: " + microserviceName);
            System.out.println("代码行数: " + linesOfCode);
            System.out.println("实体类属性总数: " + entitiesFieldCount);
            System.out.println("实体类集合: " + entityClasses);
            System.out.println("控制器类集合: " + controllerClasses);
            System.out.println("接口集合: " + interfaces);
            System.out.println("抽象类集合: " + abstractClasses);
            System.out.println("服务实现类集合: " + serviceImplementationClasses);
            System.out.println("数据传输类集合: " + dtoClasses);
            System.out.println("API 集合: " + apis);
            System.out.println("API 版本集合: " + apiVersions);
            // System.out.println("微服务间调用: " + microserviceCallResults);
            System.out.println("服务层方法调用: " + serviceMethodCallResults.get(microserviceName));
        }
        Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName); // 微服务间调用
        System.out.println("微服务间调用: " + microserviceCallResults);
        return "extra";
    }

    /*
        String pattern = "mysql://";
        Map<String, List<String>> sharedDatabases = new HashMap<>();
        Map<String, List<String>> serviceIntimacy = new HashMap<>();
        for (Configuration configuration : configurations) {
            for (String key : configuration.getItems().keySet()) {
                String value = configuration.get(key);
                String database = "";
                if (value.contains(pattern)) {
                    int startIndex = value.indexOf(pattern) + 8;
                    int endIndex = value.contains("?") ? value.indexOf("?") : value.length();
                    if (value.contains("///")) {
                        startIndex = value.indexOf("///") + 3;
                        database = "localhost:3306/" + value.substring(startIndex, endIndex);
                    }
                    else if (value.contains("127.0.0.1")){
                        startIndex = value.indexOf("//") + 2;
                        database = value.substring(startIndex, endIndex);
                        database = database.replace("localhost", "127.0.0.1");
                    }
                    else {
                        database = value.substring(startIndex, endIndex);
                    }
                    if (!sharedDatabases.containsKey(database)) {
                        sharedDatabases.put(database, new ArrayList<>());
                    }
                    sharedDatabases.get(database).add(configuration.getMicroserviceName());
                    if (!serviceIntimacy.containsKey(configuration.getMicroserviceName())) {
                        serviceIntimacy.put(configuration.getMicroserviceName(), new ArrayList<>());
                    }
                    serviceIntimacy.get(configuration.getMicroserviceName()).add(database);
                }
            }
        }
     */
}
