package com.smelldetection.controller;

import com.smelldetection.service.MetricExtraService;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@RestController
@RequestMapping("/metric")
public class MetricController {

    @Autowired
    private MetricExtraService metricExtraService;

    @GetMapping("/extra")
    public String extra(HttpServletRequest request) throws IOException, XmlPullParserException, DocumentException {
        String microserviceSystemPath = request.getParameter("path");
        metricExtraService.extraMetric(microserviceSystemPath);
        return "extra";
    }
}
