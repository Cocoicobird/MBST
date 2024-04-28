package com.smelldetection.service;

import com.smelldetection.entity.Matrix;
import com.smelldetection.entity.smell.detail.MicroserviceRankDetail;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class MicroserviceRankService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * PageRank 示例
     * G = [[0, 1, 0, 1],
     *      [0, 0, 1, 1],
     *      [1, 0, 0, 0],
     *      [0, 0, 1, 0]]
     * # G = np.matrix(G)
     * print(G)
     * M = []
     * d = 0.85
     * for i in range(1, 5):
     *     row = []
     *     for j in range(1, 5):
     *         if (G[i - 1][j - 1] == 1):
     *             row.append(1.0 / np.sum(G[i - 1]))
     *         else:
     *             row.append(0)
     *     M.append(row)
     * M = np.matrix(M)
     * print(M.T)
     * M = M.T
     * R = np.matrix([0.25, 0.25, 0.25, 0.25]).T
     * for i in range(100):
     *     R = (1 - d) / 4 + d * np.dot(M, R)
     *     print(R)
     * print(R)
     * @param filePathToMicroserviceName 微服务模块路径与微服务名称的映射
     * @param systemPath 微服务系统路径
     * @param changed 文件变更标识
     */
    public List<MicroserviceRankDetail> getMicroserviceRank(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws Exception {
        Map<String, Map<String, Integer>> microserviceCallResults = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(systemPath + "_microserviceCallResults");
        if (microserviceCallResults == null || "true".equals(changed)) {
            microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_microserviceCallResults", microserviceCallResults);
        }
        List<MicroserviceRankDetail> microserviceRank = new ArrayList<>();
        // 微服务模块路径
        List<String> filePaths = new ArrayList<>(filePathToMicroserviceName.keySet());
        int n = filePaths.size();
        if (n > 0) {
            double d = 0.85;
            // 建图
            boolean status = false;
            int[][] graph = new int[n][n];
            for (int i = 0; i < n; i++) {
                String filePath = filePaths.get(i);
                for (int j = 0; j < n; j++) {
                    String pointed = filePathToMicroserviceName.get(filePaths.get(j));
                    if (microserviceCallResults.get(filePathToMicroserviceName.get(filePath)).containsKey(pointed)) {
                        graph[i][j] = 1;
                        status = true;
                    } else {
                        graph[i][j] = 0;
                    }
                }
            }
            // 权重
            Matrix weights = new Matrix(n, n); // n * n
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (graph[i][j] == 1) {
                        weights.setValue(i, j, calculate(graph[i]));
                    } else {
                        weights.setValue(i, j, 0.0);
                    }
                }
                boolean flag = true;
                for (int j = 0; j < n; j++) {
                    if (graph[i][j] == 1) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    weights.setValue(i, i, 1);
                }
            }
            if (n == 1 || !status) {
                for (int i = 0; i < n; i++) {
                    weights.setValue(i, i, 1.0);
                }
            }
            System.out.println(weights);
            weights = weights.transpose();
            Matrix r = new Matrix(n, 1); // n * 1
            for (int i = 0; i < n; i++) {
                r.setValue(i, 0, 1.0 / n);
            }
            Matrix t = new Matrix(n, 1);
            for (int i = 0; i < n; i++) {
                t.setValue(i, 0, (1 - d) / n);
            }
            for (int i = 0; i < 100; i++) {
                r = weights.multiply(r);
                for (int j = 0; j < n; j++) {
                    double value = t.getValue(j, 0) + d * r.getValue(j, 0);
                    r.setValue(j, 0, value);
                }
            }
            for (int i = 0; i < n; i++) {
                MicroserviceRankDetail microserviceRankItem = new MicroserviceRankDetail();
                String microserviceName = filePathToMicroserviceName.get(filePaths.get(i));
                microserviceRankItem.setName(microserviceName);
                microserviceRankItem.setWeight(r.getValue(i, 0));
                microserviceRankItem.addPointers(new ArrayList<>(microserviceCallResults.get(microserviceName).keySet()));
                microserviceRank.add(microserviceRankItem);
            }
        }
        return microserviceRank;
    }

    private double calculate(int[] array) {
        double result = 0.0;
        for (double a : array) result += a;
        return 1.0 / result;
    }
}