package com.smelldetection.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.smelldetection.entity.smell.detail.CyclicReferenceDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class CyclicReferenceService {

    public CyclicReferenceDetail getCyclicReference(Map<String, String> filePathToMicroserviceName) throws IOException {
        CyclicReferenceDetail cyclicReferenceDetail = new CyclicReferenceDetail();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            List<String> classNames = new ArrayList<>();
            Map<String, Set<String>> extensionAndImplementations = new HashMap<>();
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            String packageName = FileUtils.getPackageName(filePath);
            JavaParserUtils.resolveExtensionAndImplementation(microserviceName, javaFiles, classNames,
                    extensionAndImplementations, cyclicReferenceDetail);
            for (String javaFile : javaFiles) {
                CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
                String publicClassDeclared = null;
                for (TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
                    if (typeDeclaration.isClassOrInterfaceDeclaration()) {
                        if (typeDeclaration.asClassOrInterfaceDeclaration().getFullyQualifiedName().isPresent()) {
                            publicClassDeclared = typeDeclaration.asClassOrInterfaceDeclaration().getFullyQualifiedName().get();
                        }
                    }
                }
                if (!extensionAndImplementations.containsKey(publicClassDeclared)) {
                    continue;
                }
                NodeList<ImportDeclaration> compilationUnitImports = compilationUnit.getImports();
                List<String> importedClassNames = compilationUnitImports.stream().map(ImportDeclaration::getNameAsString)
                        .filter(name -> name.startsWith(packageName)).collect(Collectors.toList());
                /**
                 * 筛选带有 * 的导入
                 */
                List<String> importedClassesWithStar = new ArrayList<>();
                for (String importedClassName : importedClassNames) {
                    if (importedClassName.endsWith("*")) {
                        List<String> starClasses = classNames.stream()
                                .filter(className -> className.startsWith(importedClassName.substring(0, importedClassName.length() - 3)))
                                .collect(Collectors.toList());
                        importedClassesWithStar.addAll(starClasses);
                    }
                    if (extensionAndImplementations.get(publicClassDeclared).contains(importedClassName)) {
                        cyclicReferenceDetail.addCyclicReference(microserviceName, publicClassDeclared, importedClassName);
                    }
                }
                for (String importedClassWithStar : importedClassesWithStar) {
                    if (extensionAndImplementations.get(publicClassDeclared).contains(importedClassWithStar)) {
                        cyclicReferenceDetail.addCyclicReference(microserviceName, publicClassDeclared, importedClassWithStar);
                    }
                }
            }
        }
        return cyclicReferenceDetail;
    }
}
