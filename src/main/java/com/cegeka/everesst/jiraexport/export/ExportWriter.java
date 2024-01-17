package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
public class ExportWriter {
    private static final Logger logger = LoggerFactory.getLogger(ExportWriter.class);
    public static final String EXPORT_FILE_NAME = "allJiraIssues.csv";
    @Value("${download.location}")
    private String downloadLocation;
    @Value("${export.columns}")
    private String columnsToExport;
    @Value("${csv.seperator}")
    private String csvSeperator;

    public void writeToFile(List<Map<String, String>> allRows) {
        List<String> lines = allRows.stream()
                .map(row -> writeRow(columnsToExport.split(","), row))
                .sorted()
                .toList();
        String header = writeHeader(columnsToExport.split(","));
        ArrayList<String> toSave = new ArrayList<>(lines);
        toSave.add(0, header);
        String filePath = downloadLocation + "/" + EXPORT_FILE_NAME;
        try {
            Files.write(Paths.get(filePath), toSave);
        } catch (IOException e) {
            logger.error("Error writing to file {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    private String writeHeader(String[] columnsToExport) {
        return Arrays.stream(columnsToExport).collect(joining(csvSeperator));
    }

    private String writeRow(String[] columns, Map<String, String> row) {
        return stream(columns).map(column -> {
            if (row.containsKey(column)) {
                return row.get(column);
            }
            return "";
        }).collect(joining(csvSeperator));
    }

}
