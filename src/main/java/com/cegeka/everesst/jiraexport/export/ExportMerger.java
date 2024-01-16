package com.cegeka.everesst.jiraexport.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.stream;

@Component
public class ExportMerger {
    @Value("${download.location}")
    private String downloadLocation;

    public void merge(int parts, ZonedDateTime startTime, ZonedDateTime endTime) {
        File downloadLocation = new File(this.downloadLocation);
        File[] files = downloadLocation.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".html"));
        List<File> downloadCsvFiles = stream(files).filter(file -> file.lastModified() > startTime.toEpochSecond() * 1000)
                .filter(file -> file.lastModified() < endTime.toEpochSecond() * 1000)
                .toList();


    }
}
