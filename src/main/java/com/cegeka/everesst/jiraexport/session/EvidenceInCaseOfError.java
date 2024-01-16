package com.cegeka.everesst.jiraexport.session;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;

public class EvidenceInCaseOfError {

    private static final Logger logger = LoggerFactory.getLogger(EvidenceInCaseOfError.class);

    public void dumpEvidence(WebDriver webDriver) {
        DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        dumpPageSource(webDriver, "log/htmlSource" + now().format(timeStampPattern) + ".html");
        takeScreenshot(webDriver, "log/screenshot" + now().format(timeStampPattern) + ".png");
    }

    private void dumpPageSource(WebDriver webDriver, String filePath) {
        try {
            String pageSource = webDriver.getPageSource();
            FileUtils.writeStringToFile(new File(filePath), pageSource, "UTF-8");
        } catch (Exception e) {
            logger.error("Error when dumping Html Page Source", e);
        }
    }

    private void takeScreenshot(WebDriver webDriver, String filePath) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) webDriver;
            File srcFile = screenshot.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));
        } catch (Exception e) {
            logger.error("Error when taking screen shot", e);
        }
    }
}
