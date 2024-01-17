package com.cegeka.everesst.jiraexport.export;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CsvExportMerger {
    public static final String CSV_FORMAT_ISSUE_KEY = "Issue key";
    public static final String CSV_FORMAT_EPIC_LINK_KEY = "Custom field (Epic Link)";

    private static final Logger logger = LoggerFactory.getLogger(CsvExportMerger.class);
    @Autowired
    private FileSelector fileSelector;

    public List<Map<String, String>> merge(int expectedParts,
                                                  ZonedDateTime startTime, ZonedDateTime endTime) {
        return fileSelector.selectFiles(FileSelector.CSV, expectedParts, startTime, endTime)
                .stream()
                .map(this::processCsvFile)
                .flatMap(List::stream)
                .toList();
    }

    private List<Map<String, String>> processCsvFile(File file) {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> records = reader.readAll();
            if (records.isEmpty()) return null;

            List<String> headers = parseHeaders(records.get(0));
            return records.stream()
                    .skip(1)
                    .map(line -> mapValues(headers, line))
                    .toList();
        } catch (Exception e) {
            logger.error("Error reading file {}", file.getPath(), e);
            throw new RuntimeException(e);
        }
    }
    private Map<String, String> mapValues(List<String> headers, String[] line) {
        Map<String, String> csvValues = new HashMap<>();
        csvValues.put(CSV_FORMAT_ISSUE_KEY, line[headers.indexOf(CSV_FORMAT_ISSUE_KEY)]);
        csvValues.put(CSV_FORMAT_EPIC_LINK_KEY, line[headers.indexOf(CSV_FORMAT_EPIC_LINK_KEY)]);
        return csvValues;
    }

    private List<String> parseHeaders(String[] headersFromCsv) {
        List<String> headers = new ArrayList<>();
        for (String header : headersFromCsv) {
            headers.add(header.trim());
        }
        return headers;
    }

}
