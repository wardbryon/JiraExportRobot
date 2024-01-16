package com.cegeka.everesst.jiraexport.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ExportMerger {
    @Value("${download.location}")
    private String downloadLocation;

    public void merge(LocalDateTime startTime, LocalDateTime endTime) {

    }
}
