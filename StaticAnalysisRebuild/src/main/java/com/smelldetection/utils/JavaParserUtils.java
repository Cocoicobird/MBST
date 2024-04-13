package com.smelldetection.utils;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.entity.item.UrlItem;
import com.smelldetection.entity.smell.detail.CyclicReferenceDetail;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class JavaParserUtils {

    /**
     * 解析 .java 文件的 API，由类上的注解路径 + 方法上的注解路径拼接
     * @param javaFile 待解析的 .java 文件
     * @param apiVersionDetail 存储 API 细节
     * @param microserviceName 待解析的 .java 所属的微服务名称
     */
    public static void resolveApiFromJavaFile(File javaFile, ApiVersionDetail apiVersionDetail, String microserviceName) throws IOException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        UrlItem urlItem = new UrlItem();
        urlItem.setFullQualifiedName(getPackageName(javaFile));
        new ClassVisitor().visit(compilationUnit, urlItem);
        new MethodAnnotationVisitor().visit(compilationUnit, urlItem);
        String preUrl = "";
        if (urlItem.getUrl1() != null) {
            preUrl = urlItem.getUrl1().substring(1, urlItem.getUrl1().length() - 1);
        }
        for (String methodName : urlItem.getUrl2().keySet()) {
            String sufUrl = urlItem.getUrl2().get(methodName);
            if ("\"\"".equals(sufUrl) && "".equals(urlItem.getHttpMethod().get(methodName))) {
                apiVersionDetail.getMissingUrl().get(microserviceName).put(methodName, preUrl);
                if (!matchApiPattern(preUrl)) {
                    apiVersionDetail.getNoVersion().get(microserviceName).put(methodName, preUrl);
                }
                continue;
            }
            sufUrl = sufUrl.substring(1, sufUrl.length() - 1);
            String httpMethod = urlItem.getHttpMethod().get(methodName);
            String url = preUrl + sufUrl + ("".equals(httpMethod) ? "" : " " + httpMethod);
            if (!matchApiPattern(url)) {
                apiVersionDetail.getNoVersion().get(microserviceName).put(methodName, url);
            }
        }
    }

    /**
     * 筛选 java 文件列表中的 api 存储到 apis 中并且筛选出 api 中的版本号
     * @param javaFiles java 文件列表
     * @param apis 存储 api
     * @return apis 中出现的版本号
     */
    public static Set<String> getApiVersions(List<String> javaFiles, Set<String> apis) throws FileNotFoundException {
        Set<String> apiVersions = new LinkedHashSet<>();
        for (String javaFile : javaFiles) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
            UrlItem urlItem = new UrlItem();
            new ClassVisitor().visit(compilationUnit, urlItem);
            new MethodAnnotationVisitor().visit(compilationUnit, urlItem);
            String preUrl = "";
            if (urlItem.getUrl1() != null) {
                preUrl = urlItem.getUrl1().substring(1, urlItem.getUrl1().length() - 1);
            }
            for (String methodName : urlItem.getUrl2().keySet()) {
                String sufUrl = urlItem.getUrl2().get(methodName);
                sufUrl = sufUrl.substring(1, sufUrl.length() - 1);
                String httpMethod = urlItem.getHttpMethod().get(methodName);
                String url = preUrl + sufUrl + ("".equals(httpMethod) ? "" : " " + httpMethod);
                apis.add(url);
                List<String> apiVersion = matchApiVersion(url);
                apiVersions.addAll(apiVersion);
            }
        }
        return apiVersions;
    }

    public static Map<String, String> getMethodToApi(File javaFile) throws FileNotFoundException {
        Map<String, String> methodToApi = new HashMap<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        UrlItem urlItem = new UrlItem();
        urlItem.setFullQualifiedName(getPackageName(javaFile));
        new ClassVisitor().visit(compilationUnit, urlItem);
        new MethodAnnotationVisitor().visit(compilationUnit, urlItem);
        String preUrl = "";
        if (urlItem.getUrl1() != null) {
            preUrl = urlItem.getUrl1().substring(1, urlItem.getUrl1().length() - 1);
        }
        for (String methodName : urlItem.getUrl2().keySet()) {
            String sufUrl = urlItem.getUrl2().get(methodName);
            if ("\"\"".equals(sufUrl) && "".equals(urlItem.getHttpMethod().get(methodName))) {
                methodToApi.put(methodName, preUrl);
                continue;
            }
            sufUrl = sufUrl.substring(1, sufUrl.length() - 1);
            String httpMethod = urlItem.getHttpMethod().get(methodName);
            String url = preUrl + sufUrl + ("".equals(httpMethod) ? "" : " " + httpMethod);
            methodToApi.put(methodName, url);
        }
        return methodToApi;
    }

    /**
     * api 版本匹配
     * @param url 单个 api
     */
    private static List<String> matchApiVersion(String url) {
        // String pattern = "/v[0-9]+/";
        String pattern = "/v\\d+(\\.\\d+)?/";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        List<String> apiVersions = new ArrayList<>();
        while (m.find()) {
            apiVersions.add(m.group());
        }
        return apiVersions;
    }

    /**
     * 获取一个 java 文件中所有方法的返回值类型
     * @param javaFile 单个 java 文件
     * @return 方法名和返回值的映射
     */
    public static Map<String, MethodDeclaration> getMethodDeclaration(String javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
        Map<String, MethodDeclaration> returnType = new HashMap<>();
        new MethodDeclarationVisitor().visit(compilationUnit, returnType);
        return returnType;
    }

    private static class MethodDeclarationVisitor extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            Map<String, MethodDeclaration> result = (Map<String, MethodDeclaration>) arg;
            result.put(n.getDeclarationAsString(false, false, true), n);
        }
    }

    private static class ClassVisitor extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            UrlItem urlItem = (UrlItem) arg;
            urlItem.setFullQualifiedName(urlItem.getFullQualifiedName() + "." + n.getNameAsString());
            if (n.getAnnotations() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    if (annotation.getClass().equals(SingleMemberAnnotationExpr.class)) {
                        if (annotation.getName().asString().equals("RequestMapping") ||
                                annotation.getName().asString().equals("PostMapping") ||
                                annotation.getName().asString().equals("GetMapping") ||
                                annotation.getName().asString().equals("PutMapping") ||
                                annotation.getName().asString().equals("DeleteMapping") ||
                                annotation.getName().asString().equals("PatchMapping")) {
                            urlItem.setUrl1(((SingleMemberAnnotationExpr) annotation).getMemberValue().toString());
                            return;
                        }
                    } else if (annotation.getClass().equals(NormalAnnotationExpr.class)) {
                        if (annotation.getName().asString().equals("RequestMapping") ||
                                annotation.getName().asString().equals("PostMapping") ||
                                annotation.getName().asString().equals("GetMapping") ||
                                annotation.getName().asString().equals("PutMapping") ||
                                annotation.getName().asString().equals("DeleteMapping") ||
                                annotation.getName().asString().equals("PatchMapping")) {
                            for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                                if (pair.getName().asString().equals("value") || pair.getName().asString().equals("path")) {
                                    urlItem.setUrl1(pair.getValue().toString());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static class MethodAnnotationVisitor extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (n.getAnnotations() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    if (annotation.getClass().equals(SingleMemberAnnotationExpr.class)) {
                        if (annotation.getName().asString().equals("RequestMapping") ||
                                annotation.getName().asString().equals("PostMapping")||
                                annotation.getName().asString().equals("GetMapping") ||
                                annotation.getName().asString().equals("PutMapping") ||
                                annotation.getName().asString().equals("DeleteMapping") ||
                                annotation.getName().asString().equals("PatchMapping")) {
                            UrlItem urlItem = (UrlItem) arg;
                            String url2 = ((SingleMemberAnnotationExpr) annotation).getMemberValue().toString();
                            String method = getHttpMethodByAnnotationType(annotation.getName().asString());
                            if (method == null)
                                method = "";
                            // urlItem.getUrl2().put(n.getName().asString(), url2 + "+" + method);
                            String fullQualifiedName = urlItem.getFullQualifiedName();
                            urlItem.getUrl2().put(fullQualifiedName + "-" + n.getName().asString(), url2);
                            urlItem.getHttpMethod().put(fullQualifiedName + "-" + n.getName().asString(), method);
                        }
                    } else if (annotation.getClass().equals(NormalAnnotationExpr.class)) {
                        if (annotation.getName().asString().equals("RequestMapping") ||
                                annotation.getName().asString().equals("PostMapping") ||
                                annotation.getName().asString().equals("GetMapping") ||
                                annotation.getName().asString().equals("PutMapping") ||
                                annotation.getName().asString().equals("DeleteMapping") ||
                                annotation.getName().asString().equals("PatchMapping")) {
                            if (((NormalAnnotationExpr) annotation).getPairs().size() == 0) {
                                UrlItem urlItem = (UrlItem) arg;
                                String method = getHttpMethodByAnnotationType(annotation.getName().asString());
                                if (method == null)
                                    method = "";
                                // urlItem.getUrl2().put(n.getName().asString(), "+" + method);
                                String fullQualifiedName = urlItem.getFullQualifiedName();
                                urlItem.getUrl2().put(fullQualifiedName + "-" + n.getName().asString(), "");
                                urlItem.getHttpMethod().put(fullQualifiedName + "-" + n.getName().asString(), method);
                                return;
                            }
                            String method = getHttpMethodByAnnotationType(annotation.getName().asString());
                            if (method == null) {
                                for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                                    if (pair.getName().asString().equals("method")) {
                                        method = pair.getValue().toString();
                                    }
                                }
                            }
                            for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                                if (pair.getName().asString().equals("value") || pair.getName().asString().equals("path")) {
                                    UrlItem urlItem = (UrlItem) arg;
                                    String fullQualifiedName = urlItem.getFullQualifiedName();
                                    // urlItem.getUrl2().put(n.getName().asString(), pair.getValue().toString() + "+" + (method == null ? "" : method));
                                    urlItem.getUrl2().put(fullQualifiedName + "-" + n.getName().asString(), pair.getValue().toString());
                                    urlItem.getHttpMethod().put(fullQualifiedName + "-" + n.getName().asString(), method);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static String getHttpMethodByAnnotationType(String annotation) {
        if ("PostMapping".equals(annotation)) {
            return "post";
        } else if ("GetMapping".equals(annotation)) {
            return "get";
        } else if ("PutMapping".equals(annotation)) {
            return "put";
        } else if ("DeleteMapping".equals(annotation)) {
            return "delete";
        } else if ("PatchMapping".equals(annotation)) {
            return "patch";
        } else {
            return null;
        }
    }

    /**
     * 判断是否符合 api
     * @param url api
     * @return 匹配为 true
     */
    public static boolean matchApiPattern(String url) {
        String pattern = "^(?!.*v\\.\\d+).*\\/v([0-9]*[a-z]*\\.*)+([0-9]|[a-z])+\\/.*$";
        Pattern p = Pattern.compile(pattern);
        return p.matcher(url).matches();
    }

    /**
     * 判断是否为实体类
     * @param javaFile 单个 .java 文件
     */
    public static boolean isEntityClass(String directory, File javaFile, Set<String> dependencies) throws IOException, DocumentException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        // JPA 框架
        Set<String> count = new LinkedHashSet<>();
        if (dependencies.contains("org.springframework.boot.spring-boot-starter-data-jpa")) {
            new EntityClassVisitor().visit(compilationUnit, count);
        }
        // mybatis 框架
        boolean flag = false;
        if (dependencies.contains("org.mybatis.spring.boot.mybatis-spring-boot-starter") || dependencies.contains("org.mybatis.mybatis")) {
            Map<String, Document> mappers = FileUtils.getMappers(directory);
            for (String mapper : mappers.keySet()) {
                Document document = mappers.get(mapper);
                Element rootElement = document.getRootElement();
                List<Element> resultMaps = new ArrayList<>();
                for (Element element : rootElement.elements()) {
                    if ("select".equals(element.getName())) {
                        if (javaFile.getAbsolutePath().contains(element.attributeValue("resultType").replace(".", "/"))
                                || javaFile.getAbsolutePath().contains(element.attributeValue("resultType").replace(".", "\\"))) {
                            flag = true;
                            break;
                        }
                    }
                    if ("resultMap".equals(element.getName())) {
                        resultMaps.add(element);
                    }
                }
                for (Element resultMap : resultMaps) {
                    if (javaFile.getAbsolutePath().contains(resultMap.attributeValue("type").replace(".", "/"))
                            || javaFile.getAbsolutePath().contains(resultMap.attributeValue("type").replace(".", "\\"))) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return !count.isEmpty() || flag || !javaFile.getAbsolutePath().toLowerCase().contains("dto");
    }

    /**
     * 统计一个 .java 中声明的成员属性的个数 注：如果一个 .java 文件中有多个类声明，则会都被统计
     * @param javaFile 单个 .java 文件
     * @return 一个 .java 成员属性个数
     */
    public static int getEntityClassFieldCount(File javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        int count = 0;
        // 一般是类或者接口
        for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
            NodeList<BodyDeclaration<?>> typeDeclarationMembers = typeDeclaration.getMembers();
            for (BodyDeclaration<?> member : typeDeclarationMembers) {
                if (member.isFieldDeclaration())
                    count++;
            }
        }
        return count;
    }

    private static class EntityClassVisitor extends VoidVisitorAdapter<Set<String>> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<String> arg) {
            if (n.getAnnotations() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    if (annotation.getNameAsString().equals("Entity")
                            || annotation.getNameAsString().equals("Document")) {
                        arg.add(annotation.getNameAsString());
                    }
                }
            }
        }
    }

    /**
     * 解析 Java 文件的继承和实现关系
     * @param microserviceName 微服务名称
     * @param javaFiles 微服务中的 .java 文件路径
     * @param classNames 存储类名
     * @param extensionAndImplementations 继承和实现关系
     * @param cyclicReferenceDetail 存储循环引用信息
     */
    public static void resolveExtensionAndImplementation(String microserviceName, List<String> javaFiles,
                                                         List<String> classNames,
                                                         Map<String, Set<String>> extensionAndImplementations,
                                                         CyclicReferenceDetail cyclicReferenceDetail) throws FileNotFoundException {
        TypeSolver typeSolver = new CombinedTypeSolver();
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.setConfiguration(new ParserConfiguration().setSymbolResolver(javaSymbolSolver));
        for (String javaFile : javaFiles) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
            for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
                String fullClassName = typeDeclaration.getFullyQualifiedName().isPresent() ?
                        typeDeclaration.getFullyQualifiedName().get() : null;
                if (fullClassName != null) {
                    classNames.add(fullClassName);
                }
            }
        }
        for (String javaFile : javaFiles) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
            for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
                String fullClassName = typeDeclaration.getFullyQualifiedName().isPresent() ?
                        typeDeclaration.getFullyQualifiedName().get() : null;
                if (fullClassName != null) {
                    if ("com.github.javaparser.ast.body.ClassOrInterfaceDeclaration".equals(typeDeclaration.getClass().getName())) {
                        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) typeDeclaration;
                        for (ClassOrInterfaceType classOrInterfaceType : classOrInterfaceDeclaration.getExtendedTypes()) {
                            for (String className : classNames) {
                                String[] str = className.split("\\.");
                                if (str[str.length - 1].equals(classOrInterfaceType.getNameAsString())) {
                                    if (!extensionAndImplementations.containsKey(className)) {
                                        extensionAndImplementations.put(className, new LinkedHashSet<>());
                                    }
                                    extensionAndImplementations.get(className).add(fullClassName);
                                    break;
                                }
                            }
                        }
                        for (ClassOrInterfaceType classOrInterfaceType : classOrInterfaceDeclaration.getImplementedTypes()) {
                            for (String className : classNames) {
                                String[] str = className.split("\\.");
                                if (str[str.length - 1].equals(classOrInterfaceType.getNameAsString())) {
                                    if (!extensionAndImplementations.containsKey(className)) {
                                        extensionAndImplementations.put(className, new LinkedHashSet<>());
                                    }
                                    extensionAndImplementations.get(className).add(fullClassName);
                                    break;
                                }
                            }
                        }
                        for (Node node : classOrInterfaceDeclaration.getChildNodes()) {
                            if ("com.github.javaparser.ast.body.ClassOrInterfaceDeclaration".equals(node.getClass().getCanonicalName())) {
                                ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) node;
                                for (ClassOrInterfaceType classOrInterfaceType : c.getExtendedTypes()) {
                                    if (classOrInterfaceType.getNameAsString().equals(classOrInterfaceDeclaration.getNameAsString())) {
                                        int length = classOrInterfaceDeclaration.getNameAsString().length();
                                        String innerFullName = fullClassName.substring(0, fullClassName.length() - length) + c.getName().asString();
                                        cyclicReferenceDetail.addCyclicReference(microserviceName, fullClassName, innerFullName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断一个 java 文件是抽象类还是接口
     * @param javaFile 单个 java 文件
     * @return 字符串 抽象类返回值为 abstract 接口返回值为 interface
     */
    public static String isAbstractClassOrInterface(File javaFile) throws FileNotFoundException {
        Set<String> type = new LinkedHashSet<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        new AbstractClassOrInterfaceVisitor().visit(compilationUnit, type);
        if (type.contains("abstract")) {
            return "abstract";
        } else if (type.contains("interface")) {
            return "interface";
        }
        return "";
    }

    private static class AbstractClassOrInterfaceVisitor extends VoidVisitorAdapter<Set<String>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<String> arg) {
            if (n.isAbstract()) {
                arg.add("abstract");
            } else if (n.isInterface()) {
                arg.add("interface");
            }
        }
    }

    /**
     * 判断一个 java 文件是否为服务实现类
     * @param javaFile 单个 java 文件
     * @return "ServiceImpl" 表示该 java 文件为服务实现类
     */
    public static String isServiceImplementationClass(File javaFile) throws FileNotFoundException {
        Set<String> type = new LinkedHashSet<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        new ServiceImplementationClassVisitor().visit(compilationUnit, type);
        return type.isEmpty() ? "" : "ServiceImpl";
    }

    private static class ServiceImplementationClassVisitor extends VoidVisitorAdapter<Set<String>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<String> arg) {
            if (n.getAnnotations() != null && n.getImplementedTypes() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    if ("Service".equals(annotation.getNameAsString())) {
                        for (ClassOrInterfaceType classOrInterfaceType : n.getImplementedTypes()) {
                            if (classOrInterfaceType.getNameAsString().contains("Service")) {
                                arg.add("ServiceImpl");
                                // System.out.println(n.getNameAsString());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取 compilationUnit 中所有的方法调用
     * @param compilationUnit 控制器类的编译单元
     * @return 含义 <对象名:<方法名:次数>>
     */
    public static Map<String, Map<String, Integer>> getAllMethodCallOfController(CompilationUnit compilationUnit) {
        List<Node> methodCallExprList = new ArrayList<>();
        resolve(compilationUnit, methodCallExprList);
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (Node node : methodCallExprList) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) node;
            if (methodCallExpr.getScope().isPresent()) {
                if (methodCallExpr.getScope().get().toNameExpr().isPresent()) {
                    NameExpr nameExpr = methodCallExpr.getScope().get().toNameExpr().get();
                    if (!result.containsKey(nameExpr.getNameAsString())) {
                        result.put(nameExpr.getNameAsString(), new HashMap<>());
                    }
                    if (result.get(nameExpr.getNameAsString()).containsKey(methodCallExpr.getNameAsString())) {
                        Integer pre = result.get(nameExpr.getNameAsString()).get(methodCallExpr.getNameAsString());
                        result.get(nameExpr.getNameAsString()).put(methodCallExpr.getNameAsString(), pre + 1);
                    } else {
                        result.get(nameExpr.getNameAsString()).put(methodCallExpr.getNameAsString(), 1);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取 MethodCallExpr 类型的节点，辅助方法
     * @param methodCallExprList 存储
     */
    private static void resolve(Node node, List<Node> methodCallExprList) {
        if (node instanceof MethodCallExpr) {
            methodCallExprList.add(node);
        }
        if (node.getChildNodes() != null) {
            for (Node childNode : node.getChildNodes()) {
                resolve(childNode, methodCallExprList);
            }
        }
    }

    /**
     * 简单过滤
     * @param compilationUnit 编译单元
     */
    public static Map<String, Map<String, Integer>> getServiceMethodCallOfController(CompilationUnit compilationUnit) {
        Map<String, Map<String, Integer>> allMethodCallOfController = getAllMethodCallOfController(compilationUnit);
        Map<String, Map<String, Integer>> serviceMethodCallOfController = new HashMap<>();
        // <变量名:类名>
        Map<String, String> fields = new HashMap<>();
        // 解析成员变量
        for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
            if (typeDeclaration.isClassOrInterfaceDeclaration()) {
                for (BodyDeclaration<?> bodyDeclaration : typeDeclaration.getMembers()) {
                    if (bodyDeclaration.isFieldDeclaration()) {
                        FieldDeclaration fieldDeclaration = bodyDeclaration.asFieldDeclaration();
                        if (fieldDeclaration.getAnnotations() != null) {
                            for (AnnotationExpr annotation : fieldDeclaration.getAnnotations()) {
                                if ("Autowired".equals(annotation.getNameAsString()) || "DubboReference".equals(annotation.getNameAsString()) || "Resource".equals(annotation.getNameAsString())) {
                                    fields.put(fieldDeclaration.getVariable(0).getNameAsString(), fieldDeclaration.getVariable(0).getTypeAsString());
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String key : allMethodCallOfController.keySet()) {
            if (key.toLowerCase().contains("service") && fields.containsKey(key)) {
                serviceMethodCallOfController.put(fields.get(key), allMethodCallOfController.get(key));
            }
        }
        return serviceMethodCallOfController;
    }

    /**
     * 判断 java 文件是否为控制器类
     * @param javaFile 单个 java 文件
     * @return 为其控制器类为 true
     */
    public static boolean isControllerClass(File javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        Set<Object> flag = new LinkedHashSet<>();
        new ControllerClassVisitor().visit(compilationUnit, flag);
        return !flag.isEmpty();
    }

    private static class ControllerClassVisitor extends VoidVisitorAdapter<Set<Object>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<Object> arg) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if ("Controller".equals(annotation.getNameAsString()) || "RestController".equals(annotation.getNameAsString())) {
                    arg.add(n);
                }
            }
        }
    }

    /**
     * 根据路径筛选数据传输类
     * @param javaFiles java 文件路径列表
     * @return 筛选出的数据传输类路径列表
     */
    public static List<String> getDtoClasses(List<String> javaFiles) {
        List<String> dtoClasses = new ArrayList<>();
        for (String javaFile : javaFiles) {
            if (javaFile.substring(javaFiles.lastIndexOf("/") == -1 ? 0 : javaFiles.lastIndexOf("/") + 1).toLowerCase().contains("dto")) {
                dtoClasses.add(javaFile);
            }
        }
        return dtoClasses;
    }

    /**
     * 判断是否为启动类
     * @param javaFile 单个 java 文件路径
     * @return 启动类为 true 否则为 false
     */
    public static boolean isStartupClass(String javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
        Set<Object> flag = new LinkedHashSet<>();
        new StartupClassVisitor().visit(compilationUnit, flag);
        return !flag.isEmpty();
    }

    private static class StartupClassVisitor extends VoidVisitorAdapter<Set<Object>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<Object> arg) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if ("SpringBootApplication".equals(annotation.getNameAsString())) {
                    arg.add(n);
                }
            }
        }
    }

    /**
     * 获取该 java 文件引入的库
     * @param javaFile 单个 java 文件
     * @return 引入的库列表
     */
    public static List<String> getImports(File javaFile) throws FileNotFoundException {
        List<String> imports = new ArrayList<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        NodeList<ImportDeclaration> compilationUnitImports = compilationUnit.getImports();
        for (ImportDeclaration importDeclaration : compilationUnitImports) {
            String importDeclarationNameAsString = importDeclaration.getNameAsString();
            imports.add(importDeclarationNameAsString);
        }
        return imports;
    }

    /**
     * 获取单个文件的所在包名
     * @param javaFile 单个 java 文件
     * @return 包名
     */
    public static String getPackageName(File javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        String packageName = "";
        if (compilationUnit.getPackageDeclaration().isPresent()) {
            PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
            packageName = packageDeclaration.getNameAsString();
        }
        return packageName;
    }
}
