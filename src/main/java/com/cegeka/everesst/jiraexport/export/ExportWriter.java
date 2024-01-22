package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

@Component
public class ExportWriter {
    private static final Logger logger = LoggerFactory.getLogger(ExportWriter.class);
    @Value("${browser.download.location}")
    private String downloadLocation;
    @Value("${export.columns}")
    private String columnsToExport;
    @Value("${export.columns.treatment}")
    private String columnsToExportTreatment;
    @Value("${export.filename}")
    private String exportFileName;
    @Value("${export.csv.seperator}")
    private String csvSeperator;



    public void writeToFile(List<Map<String, String>> allRows) {
        List<String> columns = stream(columnsToExport.split(",")).toList();
        List<ExportColumnsTreatment> columnsTreatment = stream(columnsToExportTreatment.split(",")).map(ExportColumnsTreatment::valueOf).toList();
        if(columns.size() != columnsTreatment.size()) {
            logger.error("columns {} and columnsTreatment {} should have the same size", columns.size(), columnsTreatment.size());
            throw new RuntimeException("columns and columnsTreatment should have the same size");
        }

        List<String> lines = allRows.stream()
                .map(row -> writeRow(columns, columnsTreatment, row))
                .sorted()
                .toList();
        String header = writeHeader(columns);
        ArrayList<String> toSave = new ArrayList<>(lines);
        toSave.add(0, header);
        String filePath = downloadLocation + "/" + now().format(ofPattern("yyyyMMddHHmm")) + exportFileName;
        try {
            Files.write(Paths.get(filePath), toSave);
        } catch (IOException e) {
            logger.error("Error writing to file {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    private String writeHeader(List<String> columnsToExport) {
        return columnsToExport.stream().collect(joining(csvSeperator));
    }

    private String writeRow(List<String> columns, List<ExportColumnsTreatment> columnsTreatment, Map<String, String> row) {
        return range(0, columns.size())
                .mapToObj(index -> {
                    String column = columns.get(index);
                    if (row.containsKey(column)) {
                        ExportColumnsTreatment treatment = columnsTreatment.get(index);
                        return treatment.treat(row.get(column));
                    }
                    return "";
                })
                .collect(joining(csvSeperator));

    }

}
