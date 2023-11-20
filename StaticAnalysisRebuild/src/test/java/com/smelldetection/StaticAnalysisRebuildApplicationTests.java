package com.smelldetection;

import com.smelldetection.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class StaticAnalysisRebuildApplicationTests {

    @Test
    void FileUtilsTest() throws IOException {
        List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties("F:/Java/MBST/");
        for (String fileName : applicationYamlOrProperties) {
            System.out.println(fileName);
        }
    }

}
