package com.sunnyset.jira.jiraexport.export;

import com.atlassian.jira.rest.client.api.domain.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

@Component
public class ExportWriter {
    private static final Logger logger = LoggerFactory.getLogger(ExportWriter.class);
    @Value("${export.columns}")
    private String columnsToExport;
    @Value("${export.columns.names}")
    private String columnNamesForHeader;
    @Value("${export.columns.treatment}")
    private String columnsToExportTreatment;
    @Value("${export.filename}")
    private String exportFileName;
    @Value("${export.format.csv.separator}")
    private String csvSeparator;
    @Value("${export.format.number.separator}")
    private String numberSeparator;
    @Value("${export.format.date}")
    private String dateFormat;

    record ExportConfig(String csvSeparator, String numberSeparator, String dateFormat){
    }

    private String writeHeader(ExportConfig exportConfig, List<String> columnsToExport) {
        return columnsToExport.stream().collect(joining(exportConfig.csvSeparator));
    }

    private String writeRow(ExportConfig exportConfig, List<String> columns, List<ExportColumnsTreatment> columnsTreatment, Issue issue) {
        return range(0, columns.size())
                .mapToObj(index -> {
                    String column = columns.get(index);
                        ExportColumnsTreatment treatment = columnsTreatment.get(index);
                        try{
                            return treatment.treat(issue, column, exportConfig);
                        } catch (Exception e) {
                            logger.error("Error treating column {} with treatment {}", column, treatment, e);
                            throw new RuntimeException(e);
                        }
                })
                .map(s -> s != null ? s.replace(exportConfig.csvSeparator, " ") : s)
                .collect(joining(exportConfig.csvSeparator));
    }

    public void writeToFileSystem(List<Issue> issues) {
        logger.info("Writing {} issues to file system in file {}", issues.size(), exportFileName);
        ExportConfig exportConfig = new ExportConfig(csvSeparator, numberSeparator, dateFormat);
        List<String> columns = stream(columnsToExport.split(",")).toList();
        List<String> columnNames = stream(columnNamesForHeader.split(",")).toList();
        List<ExportColumnsTreatment> columnsTreatment = stream(columnsToExportTreatment.split(",")).map(ExportColumnsTreatment::valueOf).toList();
        if(columns.size() != columnsTreatment.size() || columnNames.size() != columnsTreatment.size()) {
            logger.error("columns {} and columnsTreatment {} and columnNames {} should have the same size", columns.size(), columnsTreatment.size(), columnNames.size());
            throw new RuntimeException("columns, column names and columnsTreatment should have the same size");
        }

        List<String> lines = issues.stream()
                .map(issue -> writeRow(exportConfig, columns, columnsTreatment, issue))
                .sorted()
                .toList();
        String header = writeHeader(exportConfig, columnNames);
        writeToFileSystem(lines, header);
    }

    private void writeToFileSystem(List<String> lines, String header) {
        ArrayList<String> toSave = new ArrayList<>(lines);
        toSave.add(0, header);
        String filePath = "exports/" + now().format(ofPattern("yyyyMMddHHmm ")) + exportFileName;
        try {
            Files.write(Paths.get(filePath), toSave);
        } catch (IOException e) {
            logger.error("Error writing to file {}", filePath, e);
            throw new RuntimeException(e);
        }
    }
}
