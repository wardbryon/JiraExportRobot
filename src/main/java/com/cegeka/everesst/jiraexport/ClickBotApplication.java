package com.cegeka.everesst.jiraexport;

import com.cegeka.everesst.jiraexport.export.ExportMerger;
import com.cegeka.everesst.jiraexport.export.JiraExportDriver;
import com.cegeka.everesst.jiraexport.session.LoginDriver;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.now;

@SpringBootApplication(scanBasePackages = "com.cegeka.everesst.jiraexport.*")
@EnableConfigurationProperties
public class ClickBotApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClickBotApplication.class);
    @Autowired
    private LoginDriver sessionManager;
    @Autowired
    private WebDriver webDriver;
    @Autowired
    private JiraExportDriver jiraExportDriver;
    @Autowired
    private ExportMerger exportMerger;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClickBotApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        sessionManager.createSession(webDriver);

        ZonedDateTime startTime = ZonedDateTime.now().minusMinutes(60); // LocalDateTime.now();
        logger.info("Starting export ", startTime);
        //jiraExportDriver.sync(webDriver);
        ZonedDateTime endTime = ZonedDateTime.now(); //LocalDateTime.now();
        logger.info("Finished export ", endTime);
        exportMerger.merge(startTime, endTime);

        sessionManager.destroySession(webDriver);
    }
}
