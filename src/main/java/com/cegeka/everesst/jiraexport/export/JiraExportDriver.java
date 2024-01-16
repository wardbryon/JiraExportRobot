package com.cegeka.everesst.jiraexport.export;

import com.cegeka.everesst.jiraexport.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.cegeka.everesst.jiraexport.SeleniumUtils.backspaceMultiple;
import static com.cegeka.everesst.jiraexport.SeleniumUtils.waitABit;

@Component
public class JiraExportDriver {
    private static final Logger logger = LoggerFactory.getLogger(JiraExportDriver.class);
    @Value("${jira.filter.url}")
    private String filterUrl;

    @Value("${jira.filter}")
    private String filter;
    @Value("${jira.key.prefix}")
    private String keyPrefix;
    @Value("${jira.filter.pages}")
    private String filterPages;

    public void sync(WebDriver webDriver) {
        webDriver.get(filterUrl);
        int[] pages = Arrays.stream(filterPages.split(","))
                .mapToInt(Integer::parseInt).toArray();
        Stream<SimpleEntry<Integer, Integer>> pagesPairStream = createPairsStream(pages);

        pagesPairStream.forEach(page ->
                            extractJqlQueryResult(webDriver,
                                    "key >=" + keyPrefix+ "-" + page.getKey() + " AND key < " + keyPrefix + "-" + page.getValue() + " AND " + filter)
                        );
    }

    private static Stream<SimpleEntry<Integer, Integer>> createPairsStream(int[] numbers) {
        return IntStream.range(0, numbers.length - 1)
                .mapToObj(i -> new SimpleEntry<>(numbers[i], numbers[i + 1]));
    }

    private static void extractJqlQueryResult(WebDriver webDriver, String jql) {
        logger.info("Searching for {}", jql);
        webDriver.findElement(By.id("advanced-search")).sendKeys(backspaceMultiple(100));
        webDriver.findElement(By.id("advanced-search")).sendKeys(jql);
        webDriver.findElement(By.xpath("//button[text()='Search']")).click();
        waitABit(5);
        logger.info("Exporting results");
        webDriver.findElement(By.id("jira-export-trigger")).click();
        waitABit(2);
        SeleniumUtils.waitUntilElementPresent(webDriver, By.xpath("//a[text()='Export CSV (my defaults)']"));
        webDriver.findElement(By.xpath("//a[text()='Export CSV (my defaults)']")).click();
    }


}
