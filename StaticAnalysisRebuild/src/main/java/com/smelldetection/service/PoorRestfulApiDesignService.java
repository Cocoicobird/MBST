package com.smelldetection.service;

import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 不良的 API 设计，未遵循 RESTful 风格，未进行版本控制
 */
@Service
public class PoorRestfulApiDesignService {

    public void getPoorRestfulApiDesign(Map<String, String> filePathToMicroserviceName) throws IOException {
        // Path path = Paths.get("StaticAnalysisRebuild", "src", "main", "resources", "model", "en-sent.bin");
        // InputStream inputStream = new FileInputStream(path.toFile());
        // SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(new SentenceModel(inputStream));
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            System.out.println(microserviceName);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            Map<String, String> methodToApi = new HashMap<>();
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                methodToApi.putAll(JavaParserUtils.getMethodToApi(file));
            }
            Path tokenPath = Paths.get("StaticAnalysisRebuild", "src", "main", "resources", "model", "en-token.bin");
            InputStream tokeInputStream = new FileInputStream(tokenPath.toFile());
            TokenizerME tokenizer = new TokenizerME(new TokenizerModel(tokeInputStream));
            Path posPath = Paths.get("StaticAnalysisRebuild", "src", "main", "resources", "model", "en-pos-perceptron.bin");
            InputStream posInputStream = new FileInputStream(posPath.toFile());
            POSModel posModel = new POSModel(posInputStream);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            // 针对每一个 API
            /*
            for (String methodName : methodToApi.keySet()) {
                boolean noVersion = false; // 版本控制
                boolean hasHttpMethod = true; // HTTP 方法是否指定
                boolean hasVerb = false;
                String api = methodToApi.get(methodName);
                if (!JavaParserUtils.matchApiPattern(api)) { // 版本控制
                    noVersion = true;
                }
                String[] apiAndHttpMethod = api.split(" ");
                String[] levels = apiAndHttpMethod[0].split("/");
                if (apiAndHttpMethod.length < 2)
                    hasHttpMethod = false;
                for (String level : levels) { // API 的每一级
                    if (level.length() > 0 && level.charAt(0) == '{' && level.charAt(level.length() - 1) == '}') {
                        level = level.substring(1, level.length() - 1);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < level.length(); i++) {
                        if (Character.isUpperCase(level.charAt(i))) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(Character.toLowerCase(level.charAt(i)));
                    }
                    System.out.println(stringBuilder.toString());
                    String[] tokens = tokenizer.tokenize(stringBuilder.toString());
                    String[] tagArray = posTagger.tag(tokens);
                    for (int i = 0; i < tokens.length; i++) {
                        System.out.printf("%s -- %s%n", tokens[i], tagArray[i]);
                        if ("VB".equals(tagArray[i]) || "VBZ".equals(tagArray[i]))
                            hasVerb = true;
                    }
                }
                System.out.println(api + " " + "noVersion:" + noVersion + " hasHttpMethod:" + hasHttpMethod + " hasVerb:" + hasVerb);
            }
            */
            for (String methodName : methodToApi.keySet()) {
                boolean noVersion = false; // 版本控制
                boolean hasHttpMethod = true; // HTTP 方法是否指定
                boolean hasVerb = false;
                String api = methodToApi.get(methodName);
                if (!JavaParserUtils.matchApiPattern(api)) { // 版本控制
                    noVersion = true;
                }
                String[] apiAndHttpMethod = api.split(" ");
                String[] levels = apiAndHttpMethod[0].split("/");
                if (apiAndHttpMethod.length < 2)
                    hasHttpMethod = false;
                // 定义使用的组件 tokenize分词 ssplit断句 pos词性标注 lemma词元化 ner命名实体识别 parse语法分析 dcoref同义词分辨
                Properties properties = new Properties();
                properties.setProperty("annotators", "tokenize, ssplit, pos");
                StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
                for (String level : levels) { // API 的每一级
                    if (level.length() > 0 && level.charAt(0) == '{' && level.charAt(level.length() - 1) == '}') {
                        level = level.substring(1, level.length() - 1);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < level.length(); i++) {
                        if (Character.isUpperCase(level.charAt(i))) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(Character.toLowerCase(level.charAt(i)));
                    }
                    Annotation annotation = new Annotation(stringBuilder.toString());
                    pipeline.annotate(annotation);
                    List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
                    for (CoreLabel token : tokens) {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        if ("VB".equals(pos))
                            hasVerb = true;
                        System.out.println(word + " " + pos);
                    }
                }
                System.out.println(api + " " + "noVersion:" + noVersion + " hasHttpMethod:" + hasHttpMethod + " hasVerb:" + hasVerb);
            }
        }
    }
}