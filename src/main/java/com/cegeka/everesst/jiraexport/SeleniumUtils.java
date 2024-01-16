package com.cegeka.everesst.jiraexport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static org.openqa.selenium.Keys.BACK_SPACE;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class SeleniumUtils {

    public static CharSequence backspaceMultiple(int count) {
        return range(0, count)
                .mapToObj(i -> BACK_SPACE)
                .collect(Collectors.joining());
    }

    private void waitABit(double n) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (n * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static  void waitUntilArrivedAtPage(WebDriver webDriver, String expectedUrl) {
        new WebDriverWait(webDriver, Duration.of(10, ChronoUnit.SECONDS))
                .until(urlToBe(expectedUrl));
    }

    public static  void waitUntilElementPresent(WebDriver webDriver, By elementSelector) {
        new WebDriverWait(webDriver, Duration.of(30, ChronoUnit.SECONDS))
                .until(presenceOfElementLocated(elementSelector));
    }

    public static void waitUntilElementNoLongerPresent(WebDriver webDriver, By elementSelector) {
        new WebDriverWait(webDriver, Duration.of(30, ChronoUnit.SECONDS))
                .until(not(presenceOfElementLocated(elementSelector)));
    }

    public static void waitUntilElementNoLongerVisible(WebDriver webDriver, By elementSelector) {
        new WebDriverWait(webDriver, Duration.of(30, ChronoUnit.SECONDS))
                .until(not(visibilityOfElementLocated(elementSelector)));
    }
}
