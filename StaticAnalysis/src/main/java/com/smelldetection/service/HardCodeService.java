package com.smelldetection.service;

import com.smelldetection.base.context.HardCodeContext;
import com.smelldetection.base.factory.FileFactory;
import com.smelldetection.base.item.RequestItem;
import com.smelldetection.base.utils.HardCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: xyc
 * @date: 2022-12-15 16:40
 */
@Service
public class HardCodeService {
    @Autowired
    public FileFactory fileFactory;

    /**
     * 遍历文件，正则表达式匹配
     * @param request
     * @return
     * @throws IOException
     */
    public HardCodeContext getHardCode(RequestItem request) throws IOException {
        String path = request.getServicesPath();
        System.out.println(path);
        List<String> filesType = Arrays.asList("java","js","py");
        List<String> excludeDir = Arrays.asList("target");
        return HardCodeUtils.analysisAllFiles(path,filesType,excludeDir);
    }

    public static void main(String[] args) throws IOException {
        String packageDirName = "";
        Thread.currentThread().getContextClassLoader().getResources(packageDirName);
    }
}
