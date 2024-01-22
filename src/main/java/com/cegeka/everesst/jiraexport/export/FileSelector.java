package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.stream;

@Component
public class FileSelector {
    private static final Logger logger = LoggerFactory.getLogger(FileSelector.class);
    @Value("${browser.download.location}")
    private String downloadLocation;

    @Value("${export.filename}")
    private String exportFileName;

    public static final String HTML = ".html";
    public static final String CSV = ".csv";
    public List<File> selectFiles(String fileExtension, int expectedFileParts, ZonedDateTime startTime, ZonedDateTime endTime) {
        File downloadLocation = new File(this.downloadLocation);

        File[] files = downloadLocation.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(fileExtension));
        List<File> downloadedFiles = stream(files)
                .filter(file -> file.lastModified() > startTime.toEpochSecond() * 1000)
                .filter(file -> file.lastModified() < endTime.toEpochSecond() * 1000)
                .filter(file -> !file.getName().endsWith(exportFileName))
                .toList();
        logger.info("Selected files {}", downloadedFiles.stream().map(File::getName).toList());
        if (downloadedFiles.size() != expectedFileParts) {
            logger.error("Not all parts are downloaded, expected {} but got {}", expectedFileParts, downloadedFiles.size());
            logger.error("Continuing with the parts that are downloaded");
        }
        return downloadedFiles;
    }
}
