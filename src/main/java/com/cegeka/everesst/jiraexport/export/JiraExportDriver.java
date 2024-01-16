package com.cegeka.everesst.jiraexport.export;

import com.cegeka.everesst.jiraexport.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.time.format.DateTimeFormatter.ofPattern;

@Component
public class JiraExportDriver {

    private static final int STEP_SIZE = 1000;
    private static final Logger logger = LoggerFactory.getLogger(JiraExportDriver.class);
    @Value("${jira.filter.url}")
    private String filterUrl;

    @Value("${jira.filter}")
    private String filter;
    @Value("${jira.key.prefix}")
    private String keyPrefix;
    @Value("${jira.maximum.results}")
    private String maximumResults;

    public void sync(WebDriver webDriver) {
        webDriver.get(filterUrl);
        //webDriver.findElement(By.xpath("//a[text()='Switch to JQL']")).click();
        webDriver.findElement(By.id("advanced-search")).sendKeys(SeleniumUtils.backspaceMultiple(100));
        webDriver.findElement(By.id("advanced-search")).sendKeys(filter);
        webDriver.findElement(By.xpath("//button[text()='Search']")).click();


    }



}
