package com.smelldetection.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.smelldetection.entity.item.JavaFileParseItem;
import com.smelldetection.entity.item.ServiceCallItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 针对某一个微服务进行服务调用解析、ESBUsage 等涉及服务调用关系异味的分析
 */
public class ServiceCallParserUtils {

    // private static final String REGEX1 = "(service|Service)";
    private static final String REGEX = "\\S*(service|Service|SERVICE)";
    private static final String REGEX_IP_PORT = "(localhost|((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}):[1-9]\\d*";
    private static final double percent = 0.95;
    public static Logger logger = LogManager.getLogger(ServiceCallParserUtils.class);

    /**
     * 针对微服务调用情况进行 ESBUsage 判断
     * @param microserviceCallResults 微服务调用分析结果
     * @return 进行 ESBUsage 判断后的微服务调用情况
     */
    public static Map<String, ServiceCallItem> isESBUsageExist(Map<String, Map<String, Integer>> microserviceCallResults) {
        checkCallResult(microserviceCallResults);
        Map<String, ServiceCallItem> results = new HashMap<>();
        for (String microserviceName : microserviceCallResults.keySet()) {
            Map<String, Integer> microserviceCallItem = microserviceCallResults.get(microserviceName);
            if (!results.containsKey(microserviceName)) {
                results.put(microserviceName, new ServiceCallItem());
            }
            // 遍历该微服务的服务调用情况，calledMicroserviceName 为当前微服务所调用的微服务，即被当前微服务调用的服务
            for (String calledMicroserviceName : microserviceCallItem.keySet()) {
                results.get(microserviceName).getCallServices().add(calledMicroserviceName);
                if (!results.containsKey(calledMicroserviceName)) {
                    results.put(calledMicroserviceName, new ServiceCallItem());
                }
                results.get(calledMicroserviceName).getCalledServices().add(microserviceName);
            }
        }
        int microserviceNumber = results.size();
        for (String microserviceName : results.keySet()) {
            ServiceCallItem serviceCallItem = results.get(microserviceName);
            serviceCallItem.setMicroservice(microserviceName);
            int callServiceNumber = serviceCallItem.getCallServices().size();
            int calledServiceNumber = serviceCallItem.getCalledServices().size();
            if (callServiceNumber == calledServiceNumber && calledServiceNumber >= percent * microserviceNumber) {
                serviceCallItem.setESBUsage(true);
            }
        }
        return results;
    }

    /**
     * 获取服务调用情况
     * @param filePathToMicroserviceName 微服务路径与微服务名称的映射
     */
    public static Map<String, Map<String, Integer>> getMicroserviceCallResults(Map<String, String> filePathToMicroserviceName) {
        if (filePathToMicroserviceName.isEmpty()) {
            return null;
        }
        Map<String, Map<String, Integer>> microserviceCallResults = new HashMap<>();
        for (Map.Entry<String, String> entry : filePathToMicroserviceName.entrySet()) {
            // System.out.println(entry.getKey() + " " + entry.getValue());
            // 传入模块路径
            Map<String, Integer> parseResults = parseWebService(entry.getKey());
            // 存储解析结果
            microserviceCallResults.put(entry.getValue(), parseResults);
            // System.out.println("parseResults: " + parseResults);
        }
        return microserviceCallResults;
    }

    /**
     * 根据微服务系统中各微服务模块进行服务调用的筛选，筛除可能非本系统的服务调用
     * @param microserviceCallResults 各个微服务模块的调用结果
     */
    public static void checkCallResult(Map<String, Map<String, Integer>> microserviceCallResults) {
        // 微服务模块名称
        Set<String> microserviceNames = microserviceCallResults.keySet();
        for (Map.Entry<String, Map<String, Integer>> entry : microserviceCallResults.entrySet()) {
            // 单个微服务的服务调用情况
            Map<String, Integer> microserviceCallItem = entry.getValue();
            Iterator<Map.Entry<String, Integer>> iterator = microserviceCallItem.entrySet().iterator();
            while (iterator.hasNext()) {
                if (!microserviceNames.contains(iterator.next().getKey())) { // 微服务名称集合中不包含的服务调用
                    iterator.remove();
                }
            }
        }
        logger.info(microserviceCallResults);
    }

    /**
     * 解析微服务模块的服务调用情况
     * @param directory 微服务模块路径
     */
    private static Map<String, Integer> parseWebService(String directory) {
        Map<String, Integer> parseResultsOfMicroservice = new HashMap<>();
        Path microserviceRoot = Paths.get(directory);
        // 一次性处理整个项目的 .java 文件
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(microserviceRoot);
        projectRoot.getSourceRoots().forEach(sourceRoot -> {
            try {
                sourceRoot.tryToParse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<CompilationUnit> compilationUnits = sourceRoot.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                Map<String, Integer> parseResultsOfJavaFile = parseJavaFile(compilationUnit);
                // System.out.println("parseResultsOfJavaFile: " + parseResultsOfJavaFile);
                parseResultsOfMicroservice.putAll(parseResultsOfJavaFile);
            }
        });
        return parseResultsOfMicroservice;
    }

    /**
     * 解析单个 .java 文件
     * @param compilationUnit .java 文件的编译单元
     */
    private static Map<String, Integer> parseJavaFile(CompilationUnit compilationUnit) {
        Map<String, Integer> parseResultsOfJavaFile = new HashMap<>();
        // compilationUnit.getTypes() 获取除了 packageDeclaration 和 imports 之外的 types 节点
        // 该节点下为类、接口的声明，一般一个 .java 文件是一个类
        for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
            // members 指的是其中的类或者接口中的所有信息，包括属性、方法和内部类等都可以成为 NodeList 成员
            NodeList<BodyDeclaration<?>> typeDeclarationMembers = typeDeclaration.getMembers();
            // 存储类中的字段或者方法
            JavaFileParseItem javaFileParseItem = new JavaFileParseItem();
            // 针对每个类或者接口声明中的成员
            for (BodyDeclaration<?> typeDeclarationMember : typeDeclarationMembers) {
                // System.out.println(typeDeclarationMember.getClass().getName());
                if ("FieldDeclaration".equals(judgeNode(typeDeclarationMember))) {
                    javaFileParseItem.getFieldDeclarations().add(typeDeclarationMember.asFieldDeclaration());
                } else if ("MethodDeclaration".equals(judgeNode(typeDeclarationMember))) {
                    javaFileParseItem.getMethodDeclarations().add(typeDeclarationMember.asMethodDeclaration());
                }
            }
            // 针对上述收集的信息进行数据分析
            Map<String, Integer> callNumber = statisticalUsage(javaFileParseItem);
            parseResultsOfJavaFile.putAll(callNumber);
        }
        return parseResultsOfJavaFile;
    }

    /**
     * 判断当前节点的类型为成员变量还是方法还是其他类别
     * @param node 类内节点
     */
    private static String judgeNode(BodyDeclaration<?> node) {
        if (node instanceof FieldDeclaration) {
            return "FieldDeclaration";
        } else if (node instanceof MethodDeclaration) {
            return "MethodDeclaration";
        }
        return null;
    }

    /**
     * 解析 javaFileParseItem 存储的成员变量字段是否使用 RestTemplate
     * @param javaFileParseItem 存储一个类、接口声明中的成员变量和方法
     */
    private static Map<String, Integer> statisticalUsage(JavaFileParseItem javaFileParseItem) {
        String restTemplateName = "";
        Map<String, Integer> parseResults = new HashMap<>();
        // 解析成员字段是否使用 RestTemplate
        for (FieldDeclaration fieldDeclaration : javaFileParseItem.getFieldDeclarations()) {
            String tempType = fieldDeclaration.getElementType().toString();
            if ("RestTemplate".equals(tempType)) {
                // fieldDeclaration.getVariables() 可以获取该变量的名称以及赋值情况
                // 形式 [变量名 = 值]，故索引 0 为 变量名称
                restTemplateName = fieldDeclaration.getVariable(0).toString();
            }
        }
        // 在方法中解析其使用
        if (!"".equals(restTemplateName)) {
            // System.out.println("restTemplateName " + restTemplateName);
            parseResults = processMethods(javaFileParseItem, restTemplateName);
        }
        return parseResults;
    }

    /**
     * 从方法中处理 RestTemplate 调用
     * @param javaFileParseItem 主要是使用该类中的方法中使用 restTemplateName 的情况
     * @param restTemplateName RestTemplate 变量名
     */
    private static Map<String, Integer> processMethods(JavaFileParseItem javaFileParseItem, String restTemplateName) {
        Map<String, Integer> parseResults = new HashMap<>();
        for (int i = 0; i < javaFileParseItem.getMethodDeclarations().size(); i++) {
            // 存储表达式
            Set<Expression> callPathInit = new LinkedHashSet<>();
            // 获取初始 url 参数
            processMethodArgs(javaFileParseItem.getMethodDeclarations().get(i), callPathInit, restTemplateName);
//            System.out.println(javaFileParseItem.getMethodDeclarations().get(i));
//            System.out.println(callPathInit);
//            System.out.println(restTemplateName);
            // 解析，将其解析成形如 "http:// + ... + ..." 的形式
            for (Expression callPath : callPathInit) {
                try {
                    parseRightExpr(callPath, javaFileParseItem, i);
                } catch (Exception e) {
                    continue;
                }
            }
            Set<Expression> callPathFinal = new LinkedHashSet<>();
            // 针对字符串进行分割获取服务名
            processMethodArgs(javaFileParseItem.getMethodDeclarations().get(i), callPathFinal, restTemplateName);
            for (Expression callPath : callPathFinal) {
                String microserviceName;
                if (callPath.isStringLiteralExpr()) { // 如果是字符串
                    microserviceName = getMicroserviceNameFromURL(callPath);
                    // System.out.println("1callPath: " + callPath);
                    if (microserviceName != null) {
                        parseResults.put(microserviceName, parseResults.getOrDefault(callPath.toString(), 0) + 1);
                    }
                } else { // 形如 "http:// + ... + ..." 的形式
                    microserviceName = getMicroserviceNameStandaloneExpr(callPath);
                    // System.out.println("2callPath: " + callPath);
                    parseResults.put(microserviceName, parseResults.getOrDefault(microserviceName, 0) + 1);
                }
            }
        }
        return parseResults;
    }

    private static String getMicroserviceNameFromURL(Expression node) {
        String[] strings = node.toString().split("/");
        for (String string : strings) {
            Matcher matcher1 = Pattern.compile(REGEX).matcher(string);
            Matcher matcher2 = Pattern.compile(REGEX_IP_PORT).matcher(string);
            if (matcher1.find() || matcher2.find()) {
                return string;
            }
        }
        return null;
    }

    /**
     * 获取微服务名称
     * @param node
     */
    private static String getMicroserviceNameStandaloneExpr(Expression node) {
        String[] strings = node.toString().split("\\+|/|\"");
        for (String string : strings) {
            Matcher matcher1 = Pattern.compile(REGEX).matcher(string);
            Matcher matcher2 = Pattern.compile(REGEX_IP_PORT).matcher(string);
            if (matcher1.find() || matcher2.find()) {
                return string.replace("localhost", "127.0.0.1");
            }
        }
        return null;
    }

    /**
     * 解析出 RestTemplate 调用微服务的第一个参数，可能是 URL、String、URI 等
     * 也可能是是其他类型参数，但若是请求路径参数，只能是在第一个参数位置
     * @param callPathArgs 存储 http 调用参数
     */
    private static void processMethodArgs(Node node, Set<Expression> callPathArgs, String restTemplateName) {
        if (node instanceof MethodCallExpr) {
            // 处理方法调用所在实例对象名称
            if (((MethodCallExpr) node).getScope().isPresent()) {
                String callScope = ((MethodCallExpr) node).getScope().get().toString();
                if (restTemplateName.equals(callScope)) {
                    callPathArgs.add(((MethodCallExpr) node).getArgument(0));
                }
            }
            // 找到 RestTemplate 所调用函数的第一个参数（如果找 URL，只能是在第一个参数位置）
            return;
        }
        for (Node childNode : node.getChildNodes()) {
            processMethodArgs(childNode, callPathArgs, restTemplateName);
        }
    }

    private static void parseRightExpr(Node node, JavaFileParseItem javaFileParseItem, Integer methodDeclarationIndex) {
        //针对变量、方法、多加号连接字符串，形如 "http://" + serviceName + "/.../..." 的表达式进行解析
        if (node instanceof NameExpr) {
            // System.out.println("NameExpr");
            Expression rightExpr = getRightExprFromMethod(javaFileParseItem.getMethodDeclarations().get(methodDeclarationIndex), (Expression) node);
            // System.out.println("rightExpr: " + rightExpr);
            if (rightExpr == null) {
                // 获取变量初始化
                rightExpr = getRightExprFromField(javaFileParseItem.getFieldDeclarations(), (Expression) node);
                // System.out.println(rightExpr);
            }
            if (rightExpr != null) {
                //将其替代，在方法中
                node.replace(rightExpr);
                //将当前的set也赋值为右部
                node = rightExpr;

                if (rightExpr instanceof MethodCallExpr) {
                    parseRightExpr(node, javaFileParseItem, methodDeclarationIndex);
                }
            }
        } else if (node instanceof MethodCallExpr) {
            // 只能解析带 serviceName 参数的函数 如
            // String getService(String serviceName) {
            //      return "..." + serviceName + "...";
            // }
            if (((MethodCallExpr) node).getArguments().size() != 0){
                StringBuilder args = new StringBuilder();
                for (int i = 0; i < ((MethodCallExpr) node).getArguments().size(); i++) {
                    args.append(((MethodCallExpr) node).getArguments().get(i).toString());
                }
                // logger.info(args + "==========+++++++++++============");
                StringLiteralExpr stringLiteralExpr = new StringLiteralExpr(args.toString());
                node.replace(stringLiteralExpr);
                node = stringLiteralExpr;
            } else { // 不带参数默认是本类中 get 方法，进行处理
                Expression expression = getReturnStat(node, javaFileParseItem.getMethodDeclarations());
                Expression rightExpr =  getRightExprFromField(javaFileParseItem.getFieldDeclarations(), expression);
                node.replace(rightExpr);
                node = rightExpr;
                logger.info(rightExpr.toString());
            }
        }

        // logger.info(set.getChildNodes().size() + " " + set.getChildNodes().toString() + "==========+++++++=======");
        for (Node child : node.getChildNodes()){
            parseRightExpr(child, javaFileParseItem, methodDeclarationIndex);
        }
    }

    /**
     * 从方法中根据变量名表达式函数参数set找到对应的赋值右部表达式
     * @param node
     */
    private static Expression getRightExprFromMethod(Node node, Expression expression) {
        if (node instanceof VariableDeclarator) { // 查找声明语句
            if (((VariableDeclarator) node).getInitializer().isPresent()
                    && ((VariableDeclarator) node).getName().toString().equals(expression.toString())
                    && !"".equals(((VariableDeclarator) node).getInitializer().toString())) {
                Expression rightExpr = ((VariableDeclarator)node).getInitializer().get();
                if (rightExpr != null) {
                    return rightExpr;
                }
            }
            return null;
        } else if (node instanceof AssignExpr
                && ((AssignExpr) node).getTarget().toString().equals(expression.toString())) {
            Expression rightExpr = ((AssignExpr) node).getValue();
            if (rightExpr != null) {
                return rightExpr;
            }
            return null;
        }
        Expression rightExpr = null;
        for (Node child : node.getChildNodes()) {
            Expression rightExprFromMethod = getRightExprFromMethod(child, expression);
            if (rightExprFromMethod != null)
                rightExpr = rightExprFromMethod;
        }
        return rightExpr;
    }

    /**
     * 查看成员变量中初始化是否和 node 相同
     * @param fieldDeclarations 类中的成员变量列表
     * @param node
     */
    private static Expression getRightExprFromField(List<FieldDeclaration> fieldDeclarations, Expression node) {
        for (int i = 0; i < fieldDeclarations.size(); i++) {
            FieldDeclaration fieldDeclaration = fieldDeclarations.get(i);
            if (fieldDeclaration != null) {
                for (int j = 0; j < fieldDeclaration.getVariables().size(); j++) {
                    // System.out.println(fieldDeclaration.getVariable(j));
                    if (fieldDeclaration.getVariable(j).getName().toString().equals(node.toString())
                            && fieldDeclaration.getVariable(j).getInitializer().isPresent()) {
                        return fieldDeclaration.getVariable(j).getInitializer().get();
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param node
     * @param methodDeclarations
     */
    private static Expression getReturnStat(Node node, List<MethodDeclaration> methodDeclarations) {
        MethodCallExpr methodCallExpr = (MethodCallExpr) node;
        Expression expression = null;
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            if (methodDeclaration.getName().toString().equals(methodCallExpr.getName().toString())) {
                expression = getMethodReturn(methodDeclaration);
            }
        }
        return expression;
    }

    /**
     *
     * @param node
     */
    private static Expression getMethodReturn(Node node) {
        Expression expression = null;
        if (node instanceof ReturnStmt) {
            expression = ((ReturnStmt) node).getExpression().get();
            return expression;
        }
        for (Node child : node.getChildNodes()) {
            Expression expr = getMethodReturn(child);
            if (expr != null) {
                expression = expr;
            }
        }
        return expression;
    }

}
