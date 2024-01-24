package com.cegeka.everesst.jiraexport.export;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HtmlExportMerger {
    public static final String HTML_FORMAT_ISSUE_KEY = "Key";
    public static final String HTML_FORMAT_EPIC_LINK_ID = "Epic Link Key";

    private static final Logger logger = LoggerFactory.getLogger(HtmlExportMerger.class);
    @Autowired
    private FileSelector fileSelector;

    public List<Map<String, String>> merge(int expectedParts, ZonedDateTime startTime, ZonedDateTime endTime) {
        return fileSelector.selectFiles(FileSelector.HTML, expectedParts, startTime, endTime)
                .stream()
                .map(this::processHtmlFile)
                .flatMap(List::stream)
                .toList();
    }

    private  List<Map<String, String>> processHtmlFile(File file) {
        try {
            List<String> strings = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            String html = String.join("\n", strings);
            Document doc = Jsoup.parse(html);
            logger.info("html file parsed {}", file.getName());
            return parseRows(doc, parseHeaders(doc));
        } catch (IOException e) {
            logger.error("Error reading file {}", file.toPath(), e);
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, String>> parseRows(Document doc, List<String> headers) {
        Elements rowElements = doc.select("table#issuetable tbody tr");
        List<Map<String, String>> rows = new ArrayList<>();

        for (Element row : rowElements) {
            Elements cellElements = row.select("td");
            Map<String, String> rowMap = new HashMap<>();
            for (int i = 0; i < cellElements.size(); i++) {
                String header = headers.get(i);
                String value = cellElements.get(i).text().trim();
                rowMap.put(header, value);
            }
            rows.add(rowMap);
        }
        logger.info("File parsed with {} entries", rows.size());
        return rows;
    }

    private List<String> parseHeaders(Document doc) {
        Elements headerElements = doc.select("table#issuetable thead th");
        List<String> headers = new ArrayList<>();
        for (Element header : headerElements) {
            headers.add(header.text().trim());
        }
        logger.info("headers parsed from HTML {}", headers);
        return headers;
    }
}
