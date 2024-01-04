package com.smelldetection.service;

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
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class MetricExtraService {

    public void extraMetric(String microserviceSystemPath) throws IOException, XmlPullParserException {
        System.out.println("----------------------------------------------------------------");
        System.out.println("当前项目地址: " + microserviceSystemPath);
        List<String> microservicePaths = FileUtils.getServices(microserviceSystemPath);
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(microserviceSystemPath);
        // 针对每一个微服务
        for (String microservicePath : microservicePaths) {
            List<String> javaFiles = FileUtils.getJavaFiles(microservicePath); // main 下所有 .java 文件
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(microservicePath); // 配置文件
            List<String> pomXml = FileUtils.getPomXml(microservicePath); // 依赖文件
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
                configurations.add(configuration);
            }
            // 解析依赖文件
            List<Pom> poms = new ArrayList<>();
            for (String p : pomXml) {
                Pom pom = new Pom();
                MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
                Model model = mavenXpp3Reader.read(new FileInputStream(p));
                pom.setMavenModel(model);
                poms.add(pom);
            }
            Set<String> dependencies = new LinkedHashSet<>(); // 非 Spring 官方依赖
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
            Set<String> dataBases = new LinkedHashSet<>();
            getDataBases(dataBases, configurations);
            // <微服务名称:<Service对象:<方法名:次数>>>
            Map<String, Map<String, Map<String, Integer>>> serviceMethodCallResults = new HashMap<>();
            String microserviceName = filePathToMicroserviceName.get(microservicePath);
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
                    // 统计 service 层方法调用次数
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
            System.out.println("当前微服务: " + microserviceName);
            System.out.println("代码行数: " + linesOfCode);
            System.out.println("实体类集合(个数:" + entityClasses.size() + "): " + entityClasses);
            System.out.println("实体类属性总数: " + entitiesFieldCount);
            System.out.println("实体类平均属性个数: " + (entityClasses.isEmpty() ? 0 : entitiesFieldCount * 1.0 / entityClasses.size()));
            System.out.println("控制器类集合(个数:" + controllerClasses.size() + "): " + controllerClasses);
            System.out.println("接口集合(个数:" + interfaces.size() + "): " + interfaces);
            System.out.println("抽象类集合(个数:" + abstractClasses.size() + "): " + abstractClasses);
            System.out.println("服务实现类集合(个数:" + serviceImplementationClasses.size() + "): " + serviceImplementationClasses);
            System.out.println("数据传输类集合(个数:" + dtoClasses.size() + "): " + dtoClasses);
            System.out.println("API 集合(个数:" + apis.size() + "): " + apis);
            System.out.println("API 版本集合(个数:" + apiVersions.size() + "): " + apiVersions);
            System.out.println("使用的数据库(个数:" + dataBases.size() + "): " + dataBases);
            System.out.println("服务层方法调用: " + serviceMethodCallResults.get(microserviceName));
        }
        Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName); // 微服务间调用
        System.out.println("微服务间调用: " + microserviceCallResults);
    }

    private void getDataBases(Set<String> dataBases, List<Configuration> configurations) {
        String pattern = "mysql://";
        for (Configuration configuration : configurations) {
            for (String key : configuration.getItems().keySet()) {
                String value = configuration.get(key);
                String dataBase = "";
                if (value.contains(pattern)) {
                    int startIndex = value.indexOf(pattern) + 8;
                    int endIndex = value.contains("?") ? value.indexOf("?") : value.length();
                    if (value.contains("///")) {
                        startIndex = value.indexOf("///") + 3;
                        dataBase = "localhost:3306/" + value.substring(startIndex, endIndex);
                    }
                    else if (value.contains("127.0.0.1")){
                        startIndex = value.indexOf("//") + 2;
                        dataBase = value.substring(startIndex, endIndex);
                        dataBase = dataBase.replace("localhost", "127.0.0.1");
                    }
                    else {
                        dataBase = value.substring(startIndex, endIndex);
                    }
                }
                if (!"".equals(dataBase)) {
                    dataBases.add(dataBase);
                }
            }
        }
    }
}
