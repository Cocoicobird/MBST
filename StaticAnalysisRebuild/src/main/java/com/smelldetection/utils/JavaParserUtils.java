package com.smelldetection.utils;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.entity.item.UrlItem;
import com.smelldetection.entity.smell.detail.CyclicReferenceDetail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
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
        new ClassVisitor().visit(compilationUnit, urlItem);
        new MethodVisitor().visit(compilationUnit, urlItem);
        String preUrl = "";
        if (urlItem.getUrl1() != null) {
            preUrl = urlItem.getUrl1().substring(1, urlItem.getUrl1().length() - 1);
        }
        for (String methodName : urlItem.getUrl2().keySet()) {
            String sufUrl = urlItem.getUrl2().get(methodName);
            if ("".equals(sufUrl)) {
                apiVersionDetail.getMissingUrl().get(microserviceName).put(methodName, preUrl);
                if (!matchApiPattern(preUrl)) {
                    apiVersionDetail.getNoVersion().get(microserviceName).put(methodName, preUrl);
                }
                continue;
            }
            sufUrl = sufUrl.substring(1, sufUrl.length() - 1);
            String url = preUrl + sufUrl;
            if (!matchApiPattern(url)) {
                apiVersionDetail.getNoVersion().get(microserviceName).put(methodName, url);
            }
        }
    }

    private static class ClassVisitor extends VoidVisitorAdapter<Object> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            if (n.getAnnotations() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    if (annotation.getClass().equals(SingleMemberAnnotationExpr.class)) {
                        if (annotation.getName().asString().equals("RequestMapping") ||
                                annotation.getName().asString().equals("PostMapping") ||
                                annotation.getName().asString().equals("GetMapping") ||
                                annotation.getName().asString().equals("PutMapping") ||
                                annotation.getName().asString().equals("DeleteMapping") ||
                                annotation.getName().asString().equals("PatchMapping")) {
                            UrlItem urlItem = (UrlItem) arg;
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
                                    UrlItem urlItem = (UrlItem) arg;
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

    private static class MethodVisitor extends VoidVisitorAdapter<Object> {

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
                            urlItem.getUrl2().put(n.getName().asString(), url2);

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
                                urlItem.getUrl2().put(n.getName().asString(), "");
                                return;
                            }
                            for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                                if (pair.getName().asString().equals("value") || pair.getName().asString().equals("path")) {
                                    UrlItem urlItem = (UrlItem) arg;
                                    urlItem.getUrl2().put(n.getName().asString(), pair.getValue().toString());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean matchApiPattern(String url) {
        String pattern = "^(?!.*v\\.\\d+).*\\/v([0-9]*[a-z]*\\.*)+([0-9]|[a-z])+\\/.*$";
        Pattern p = Pattern.compile(pattern);
        return p.matcher(url).matches();
    }

    /**
     * 判断是否为实体类
     * @param javaFile .java 文件
     */
    public static boolean isEntityClass(File javaFile) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        Set<String> count = new LinkedHashSet<>();
        new EntityClassVisitor().visit(compilationUnit, count);
        return !count.isEmpty();
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


}
