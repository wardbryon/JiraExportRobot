package com.cegeka.everesst.jiraexport;

import com.cegeka.everesst.jiraexport.session.LoginDriver;
import com.cegeka.everesst.jiraexport.export.JiraExportDriver;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static java.time.LocalDate.parse;

@SpringBootApplication(scanBasePackages = "com.cegeka.everesst.tempocloudsync.*")
@EnableConfigurationProperties
public class ClickBotApplication implements CommandLineRunner {
    @Autowired
    private LoginDriver sessionManager;
    @Autowired
    private WebDriver webDriver;
    @Autowired
    private JiraExportDriver jiraExportDriver;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClickBotApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        sessionManager.createSession(webDriver);
        jiraExportDriver.sync(webDriver);

        sessionManager.destroySession(webDriver);
    }
}
