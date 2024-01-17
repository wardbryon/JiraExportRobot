package com.cegeka.everesst.jiraexport.export;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
public class ExportMerger {

    private static final Logger logger = LoggerFactory.getLogger(ExportMerger.class);
    @Value("${download.location}")
    private String downloadLocation;

    public void merge(int parts, ZonedDateTime startTime, ZonedDateTime endTime) {
        File downloadLocation = new File(this.downloadLocation);

        File[] files = downloadLocation.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".html"));
        List<File> downloadedFiles = stream(files).filter(file -> file.lastModified() > startTime.toEpochSecond() * 1000)
                .filter(file -> file.lastModified() < endTime.toEpochSecond() * 1000)
                .toList();
        if (downloadedFiles.size() != parts) {
            logger.error("Not all parts are downloaded, expected {} but got {}", parts, downloadedFiles.size());
            logger.error("Continuing with the parts that are downloaded");
        }
        List<MergeInputFile> toMerge = new ArrayList<>();
        downloadedFiles.forEach(file -> {
                    try {
                        List<String> strings = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                        String html = strings.stream().collect(joining("\n"));
                        Document doc = Jsoup.parse(html);
                        toMerge.add(parseRows(doc, parseHeaders(doc)));
                        logger.info("html file parsed");
                    } catch (IOException e) {
                        logger.error("Error reading file {}", file.toPath(), e);
                        throw new RuntimeException(e);
                    }
                }
        );
        List<Map<String, String>> allRows = toMerge.stream().map(MergeInputFile::getRows).flatMap(List::stream).toList();
        writeToFile(allRows);

    }

    public void writeToFile(List<Map<String, String>> allRows) {
        String filePath = downloadLocation + "/allJiraIssues.csv";
        List<String> lines = allRows.stream().map(row -> row.get("Key") + ";" + row.get("EverESSt Domain")).sorted().toList();

        try {
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MergeInputFile parseRows(Document doc, List<String> headers) {
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
        return new MergeInputFile(rows);
    }

    private static List<String> parseHeaders(Document doc) {
        Elements headerElements = doc.select("table#issuetable thead th");
        List<String> headers = new ArrayList<>();
        for (Element header : headerElements) {
            headers.add(header.text().trim());
        }
        return headers;
    }
}
