package com.smelldetection.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.smelldetection.entity.MetricSummary;
import com.smelldetection.service.MetricExtraService;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@RestController
@RequestMapping("/metric")
public class MetricController {

    @Autowired
    private MetricExtraService metricExtraService;

    @PostMapping("/extra")
    public String extra(HttpServletRequest request) throws IOException, XmlPullParserException, DocumentException {
        String microserviceSystemPath = request.getParameter("path");
        metricExtraService.extraMetric(microserviceSystemPath);
        return "extra";
    }

    @GetMapping("/list")
    public List<MetricSummary> list(HttpServletRequest request) {
        if (request.getParameter("path") == null)
            return metricExtraService.getMetricSummaries();
        return metricExtraService.getMetricSummaries(request.getParameter("path"));
    }

    @GetMapping("/excel")
    public void exportMetricSummaryList(HttpServletResponse response) throws IOException {
        setExcelResponseProperty(response, "data");
        List<MetricSummary> metricSummaryList = metricExtraService.getMetricSummaries();
        EasyExcel.write(response.getOutputStream())
                .head(MetricSummary.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("data")
                .doWrite(metricSummaryList);
    }

    private void setExcelResponseProperty(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }
}
