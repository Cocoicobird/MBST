package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ApiDesignDetail;
import com.smelldetection.entity.smell.detail.NoHealthCheckAndNoServiceDiscoveryPatternDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.NlpUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 不良的 API 设计，未遵循 RESTful 风格，未进行版本控制
 */
@Service
public class PoorRestfulApiDesignService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ApiDesignDetail getPoorRestfulApiDesign(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        ApiDesignDetail apiDesignDetail = new ApiDesignDetail();
        Map<String, Map<String, String>> urls;
        if (redisTemplate.opsForValue().get(systemPath + "_urls") != null || "true".equals(changed)) {
            urls = (Map<String, Map<String, String>>) redisTemplate.opsForValue().get(systemPath + "_urls");
        } else {
            urls = new HashMap<>();
        }
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            System.out.println(microserviceName);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            Map<String, String> methodToApi;
            if (!urls.containsKey(microserviceName)) {
                methodToApi = new HashMap<>();
                for (String javaFile : javaFiles) {
                    File file = new File(javaFile);
                    methodToApi.putAll(JavaParserUtils.getMethodToApi(file));
                }
            } else {
                methodToApi = urls.get(microserviceName);
            }
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
                StanfordCoreNLP pipeline = NlpUtils.pipeline;
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
                if (noVersion)
                    apiDesignDetail.putNoVersion(microserviceName, methodName, api);
                if (!hasHttpMethod)
                    apiDesignDetail.putMissingHttpMethod(microserviceName, methodName, api);
                if (hasVerb)
                    apiDesignDetail.putNoStandard(microserviceName, methodName, api);
                System.out.println(api + " " + "noVersion:" + noVersion + " hasHttpMethod:" + hasHttpMethod + " hasVerb:" + hasVerb);
            }
        }
        redisTemplate.opsForValue().set(systemPath + "_urls", urls);
        redisTemplate.opsForValue().set(systemPath + "_poorRestfulApiDesign_" + start, apiDesignDetail);
        return apiDesignDetail;
    }

    public List<ApiDesignDetail> getPoorRestfulApiDesignHistory(String systemPath) {
        String key = systemPath + "_poorRestfulApiDesign_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ApiDesignDetail> apiDesignDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                apiDesignDetails.add((ApiDesignDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return apiDesignDetails;
    }
}