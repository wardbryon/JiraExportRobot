package com.cegeka.everesst.jiraexport.export;

import com.cegeka.everesst.jiraexport.session.EvidenceInCaseOfError;
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

import static com.cegeka.everesst.jiraexport.SeleniumUtils.*;
import static java.lang.Integer.parseInt;

@Component
public class JiraExportDriver {
    private static final Logger logger = LoggerFactory.getLogger(JiraExportDriver.class);
    @Value("${browser.wait.for.download}")
    public String waitUntilLastDownloadIsFinished;

    @Value("${browser.wait.between.exports}")
    public String waitBetweenExports;
    @Value("${jira.filter.url}")
    private String filterUrl;

    @Value("${jira.filter}")
    private String filter;
    @Value("${jira.key.prefix}")
    private String keyPrefix;
    @Value("${jira.filter.pages}")
    private String filterPages;
    private static final By advancedSearchTextbox = By.id("advanced-search");
    private static final By searchButton = By.xpath("//button[text()='Search']");
    private static final By exportButton = By.id("jira-export-trigger");
    private static final By exportToCsv = By.xpath("//a[text()='Export CSV (my defaults)']");
    private static final By exportToHtml = By.xpath("//a[text()='Export HTML report (my defaults)']");

    public int sync(WebDriver webDriver) {
        webDriver.get(filterUrl);
        int[] pages = Arrays.stream(filterPages.split(","))
                .mapToInt(Integer::parseInt).toArray();
        Stream<SimpleEntry<Integer, Integer>> pagesPairStream = createPaginationPairs(pages);

        pagesPairStream.forEach(page ->
                            extractJqlQueryResult(webDriver,
                                    "key >=" + keyPrefix+ "-" + page.getKey() +
                                        " AND key < " + keyPrefix + "-" + page.getValue() +
                                        " AND " + filter)
                        );
        extractJqlQueryResult(webDriver,
                "key >=" + keyPrefix+ "-" + pages[pages.length-1] +
                    " AND " + filter);
        waitABit(waitUntilLastDownloadIsFinished());
        return createPaginationPairs(pages).toList().size() + 1;
    }

    private Stream<SimpleEntry<Integer, Integer>> createPaginationPairs(int[] numbers) {
        return IntStream.range(0, numbers.length - 1)
                .mapToObj(i -> new SimpleEntry<>(numbers[i], numbers[i + 1]));
    }

    private void extractJqlQueryResult(WebDriver webDriver, String jql) {
        try{
            logger.info("Searching for {}", jql);
            webDriver.findElement(advancedSearchTextbox).sendKeys(backspaceMultiple(300));
            webDriver.findElement(advancedSearchTextbox).sendKeys(jql);
            webDriver.findElement(searchButton).click();
            waitABit(waitBetweenExports());
            exportAsHtmlAndCsvForEpicLinks(webDriver);
        }catch (Exception e){
            new EvidenceInCaseOfError().dumpEvidence(webDriver);
        }
    }

    private int waitBetweenExports() {
        return parseInt(waitBetweenExports);
    }

    private int waitUntilLastDownloadIsFinished() {
        return parseInt(waitUntilLastDownloadIsFinished);
    }

    private void exportAsHtmlAndCsvForEpicLinks(WebDriver webDriver) {
        logger.info("Exporting results as CSV & HTML");
        exportType(webDriver, exportToHtml);
        waitABit(waitBetweenExports());
        exportType(webDriver, exportToCsv);
        waitABit(waitBetweenExports());
    }

    private void exportType(WebDriver webDriver, By type) {
        webDriver.findElement(exportButton).click();
        waitABit(waitBetweenExports());
        waitUntilElementPresent(webDriver, type);
        webDriver.findElement(type).click();
    }
}
