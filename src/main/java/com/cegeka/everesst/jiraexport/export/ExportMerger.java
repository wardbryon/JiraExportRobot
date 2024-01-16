package com.cegeka.everesst.jiraexport.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExportMerger {
    @Value("${download.location}")
    private String downloadLocation;

    public void merge(ZonedDateTime startTime, ZonedDateTime endTime) {
        File downloadLocation = new File(this.downloadLocation);
        File[] files = downloadLocation.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".csv"));
        List<File> downloadCsvFiles = Arrays.stream(files).filter(file -> file.lastModified() > startTime.toEpochSecond() * 1000)
                .filter(file -> file.lastModified() < endTime.toEpochSecond() * 1000)
                .toList();

    }
}
