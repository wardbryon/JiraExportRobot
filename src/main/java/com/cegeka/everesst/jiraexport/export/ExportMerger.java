package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.cegeka.everesst.jiraexport.export.CsvExportMerger.CSV_FORMAT_EPIC_LINK_KEY;
import static com.cegeka.everesst.jiraexport.export.CsvExportMerger.CSV_FORMAT_ISSUE_KEY;
import static com.cegeka.everesst.jiraexport.export.HtmlExportMerger.HTML_FORMAT_EPIC_LINK_ID;
import static com.cegeka.everesst.jiraexport.export.HtmlExportMerger.HTML_FORMAT_ISSUE_KEY;

@Component
public class ExportMerger {

    private static final Logger logger = LoggerFactory.getLogger(ExportMerger.class);

    @Autowired
    private HtmlExportMerger htmlExportMerger;
    @Autowired
    private CsvExportMerger csvExportMerger;
    @Autowired
    private ExportWriter exportWriter;


    public void merge(int parts, ZonedDateTime startTime, ZonedDateTime endTime) {
        List<Map<String, String>> jiraItemsFromHtml = htmlExportMerger.merge(parts, startTime, endTime);
        List<Map<String, String>> jiraItemsFromCsv = csvExportMerger.merge(parts, startTime, endTime);
        if(!jiraItemsFromCsv.isEmpty() && jiraItemsFromCsv.size() != jiraItemsFromHtml.size()) {
            logger.error("jiraItemsFromCsv {} and jiraItemsFromHtml {} should have the same size", jiraItemsFromCsv.size(), jiraItemsFromHtml.size());
            throw new RuntimeException("jiraItemsFromCsv and jiraItemsFromHtml should have the same size");
        }
        insertEpicLinkKey(jiraItemsFromHtml, jiraItemsFromCsv);
        exportWriter.writeToFile(jiraItemsFromHtml);
    }

    private void insertEpicLinkKey(List<Map<String, String>> jiraItemsFromHtml, List<Map<String, String>> jiraItemsFromCsv) {
        logger.info("merging htmlExport and csvExport for the Epic Link Key");
         jiraItemsFromHtml.forEach(htmlItem -> {
            String key = htmlItem.get(HTML_FORMAT_ISSUE_KEY);
            jiraItemsFromCsv.stream()
                    .filter(csvItem -> csvItem.get(CSV_FORMAT_ISSUE_KEY).equals(key))
                    .findFirst()
                    .ifPresent(csvItem -> htmlItem.put(HTML_FORMAT_EPIC_LINK_ID, csvItem.get(CSV_FORMAT_EPIC_LINK_KEY)));
        });
    }

}
