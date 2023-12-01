package com.smelldetection.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.entity.item.UrlItem;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class ApiParserUtils {

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
}
