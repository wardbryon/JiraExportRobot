package com.cegeka.everesst.jiraexport.session;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.cegeka.everesst.jiraexport.SeleniumUtils.waitUntilArrivedAtPage;
import static com.cegeka.everesst.jiraexport.SeleniumUtils.waitUntilElementPresent;

@Component
public class LoginDriver {

    private static final Logger logger = LoggerFactory.getLogger(LoginDriver.class);
    @Value("${jira.login.url}")
    private String url;
    @Value("${jira.login.mail.address}")
    private String mailAddress;

    public Set<Cookie> createSession(WebDriver webDriver) {
        try {
            webDriver.get(url);
            webDriver.findElement(By.xpath("//span[@aria-label='Sign in']")).click();
            logger.info("At the login page of Jira Cloud");
            waitUntilElementPresent(webDriver, By.id("microsoft-auth-button"));
            webDriver.findElement(By.id("microsoft-auth-button")).click();
            waitUntilElementPresent(webDriver, By.xpath("//div[@data-test-id='" + mailAddress + "']"));
            webDriver.findElement(By.xpath("//div[@data-test-id='" + mailAddress + "']")).click();
            waitUntilArrivedAtPage(webDriver, url);
            logger.info("Succesfully logged in");
            return webDriver.manage().getCookies();
        }catch (Exception e){
            new EvidenceInCaseOfError().dumpEvidence(webDriver);
        }
        return Set.of();
    }



    public void destroySession(WebDriver webDriver) {
        webDriver.quit();
    }
}
