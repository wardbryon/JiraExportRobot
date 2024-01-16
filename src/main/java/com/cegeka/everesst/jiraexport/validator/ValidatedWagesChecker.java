package com.cegeka.everesst.jiraexport.validator;

import com.cegeka.everesst.jiraexport.session.EvidenceInCaseOfError;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

@Component
public class ValidatedWagesChecker {

    private static final Logger logger = LoggerFactory.getLogger(ValidatedWagesChecker.class);
    //@Value("${apollo.url}")
    private String url;


    private final String searchWerkgeverField = "p-dropdown[data-testid='employer-id-search-input'] p-overlay input:first-of-type";
    private final String resetButton = "button[data-testid='search-reset-button']";
    private final String searchButton = ".search-buttons > button:nth-of-type(2)";
    private final String werkgeverDropdown = "p-dropdown[data-testid='employer-id-search-input']";

    public void searchAcertaSleutel(WebDriver webDriver, String acertaSleutel) {
        String werkgeverSelection = "";
        try {
            webDriver.get(url);

            new WebDriverWait(webDriver, Duration.of(10, ChronoUnit.SECONDS))
                    .until(presenceOfElementLocated(By.cssSelector(werkgeverDropdown)));

            webDriver.findElement(By.cssSelector(werkgeverDropdown))
                    .click();
            webDriver.findElement(By.cssSelector(searchWerkgeverField))
                    .sendKeys(acertaSleutel);

            new WebDriverWait(webDriver, Duration.of(2, ChronoUnit.SECONDS))
                    .until(presenceOfElementLocated(xpathToClickToEmployer(acertaSleutel)));

            werkgeverSelection = webDriver.findElement(xpathToClickToEmployer(acertaSleutel)).getText();

            WebElement employerResult = webDriver.findElement(xpathToClickToEmployer(acertaSleutel));
            String werkgeverNaam = extractWerkgeverNaam(employerResult.getText());
            employerResult.click();

            new WebDriverWait(webDriver, Duration.of(2, ChronoUnit.SECONDS))
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(searchButton)));

            webDriver.findElement(By.cssSelector(searchButton))
                    .click();

            new WebDriverWait(webDriver, Duration.of(2, ChronoUnit.SECONDS))
                    .until(presenceOfElementLocated(By.cssSelector("p-table[data-testid='table']")));

            Optional<LocalDateTime> gevalideerd = parseDateTime(webDriver.findElements(By.cssSelector("tr[data-testid='complete-wage-finalization-row'] > td:nth-of-type(4)")).get(0).getText());
            Optional<LocalDateTime> gefinaliseerd = parseDateTime(webDriver.findElements(By.cssSelector("tr[data-testid='complete-wage-finalization-row'] > td:nth-of-type(5)")).get(0).getText());
            logger.info("Werkgever met Acerta sleutel {} gevalideerd op {}", acertaSleutel, gevalideerd);
            logger.info("Werkgever met Acerta sleutel {} gefinaliseerd op {}", acertaSleutel, gefinaliseerd);

            webDriver.findElement(By.cssSelector(resetButton))
                    .click();
        } catch (Exception e) {
            new EvidenceInCaseOfError().dumpEvidence(webDriver);
            logger.error("Exception",e);
            if (werkgeverSelection.contains("No results found")) {
                logger.error("Werkgever {} niet gevonden in de werkgever dropdown", acertaSleutel);
            } else {
                logger.error("Onbekende fout bij het opzoeken van werkgever sleutel {}, mogelijk is de pagina & id's veranderd", acertaSleutel);
            }
        }
    }

    private String dateTimeOrEmptyString(Optional<LocalDateTime> dateTime, DateTimeFormatter format) {
        return dateTime.map(localDateTime -> localDateTime.format(format)).orElse("");
    }

    private Optional<LocalDateTime> parseDateTime(String dateString) {
        if("".equals(dateString.trim())){
            return Optional.empty();
        }else{
            DateTimeFormatter formatter = ofPattern("dd/MM/yyyy H:mm:ss");
            return Optional.of(LocalDateTime.parse(dateString, formatter));
        }
    }

    private static String extractWerkgeverNaam(String werkgeverNaam) {
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(werkgeverNaam);
        if(matcher.find()){
            werkgeverNaam = matcher.group(1);
        }
        return werkgeverNaam;
    }

    private static By xpathToClickToEmployer(String acertaSleutel) {
        return By.xpath("//li[contains(., '" + acertaSleutel + " (')]");
    }

}
