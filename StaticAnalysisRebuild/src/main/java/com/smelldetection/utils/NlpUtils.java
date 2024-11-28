package com.smelldetection.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class NlpUtils {

    public static final Properties properties = new Properties();
    public static StanfordCoreNLP pipeline;

    static {
        System.out.println("NLP-----------------");
        // 定义使用的组件 tokenize分词 ssplit断句 pos词性标注 lemma词元化 ner命名实体识别 parse语法分析 dcoref同义词分辨
        properties.setProperty("annotators", "tokenize, ssplit, pos");
        pipeline = new StanfordCoreNLP(properties);
    }

    /**
     * 提取文本中的单词
     * @param text 文本
     * @return 单词列表
     */
    private static List<String> extractWordFromText(String text) {
        List<String> result = new ArrayList<>();
        if (text.length() == 0)
            return result;
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens) {
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            result.add(word);
        }
        return result;
    }

    /**
     * 计算词频并丰富词汇表
     * @param words 单个文本的词汇
     * @param vocabulary 词汇表，保存所有文本中出现的词汇
     * @return 单个文本的词频
     */
    private static Map<String, Double> calculateFrequency(List<String> words, List<String> vocabulary) {
        Map<String, Integer> count = new HashMap<>();
        for (String word : words) {
            if (!count.containsKey(word)) {
                count.put(word, 0);
            }
            count.put(word, count.get(word) + 1);
            if (!vocabulary.contains(word)) {
                vocabulary.add(word);
            }
        }
        int total = words.size();
        Map<String, Double> result = new HashMap<>();
        for (String word : count.keySet()) {
            result.put(word, 1.0 * count.get(word) / total);
        }
        return result;
    }

    /**
     * 计算词向量
     * @param frequencies 单个文本的词频
     * @param vocabulary 词汇表
     * @return 词向量，与词汇表同维度
     */
    private static List<Double> frequencyVector(Map<String, Double> frequencies, List<String> vocabulary) {
        List<Double> vector = new ArrayList<>();
        for (String word : vocabulary) {
            double value = 0;
            if (frequencies.containsKey(word))
                value = frequencies.get(word);
            vector.add(value);
        }
        return vector;
    }

    private static double getSquareSum(List<Double> vector) {
        double result = 0;
        for (Double value : vector) {
            result += value * value;
        }
        return Math.sqrt(result);
    }

    private static double getCosine(List<Double> vector1, List<Double> vector2) {
        double sqrt1 = getSquareSum(vector1);
        double sqrt2 = getSquareSum(vector2);
        double result = 0;
        for (int i = 0; i < vector1.size(); i++) {
            result += vector1.get(i) * vector2.get(i);
        }
        return result / (sqrt1 * sqrt2);
    }

    public static Double getSimilarity(String text1, String text2) {
        List<String> words1 = extractWordFromText(text1);
        List<String> words2 = extractWordFromText(text2);
        List<String> vocabulary = new ArrayList<>();
        Map<String, Double> frequencies1 = calculateFrequency(words1, vocabulary);
        Map<String, Double> frequencies2 = calculateFrequency(words2, vocabulary);
        return getCosine(frequencyVector(frequencies1, vocabulary), frequencyVector(frequencies2, vocabulary));
    }
}
