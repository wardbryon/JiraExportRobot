package com.cegeka.everesst.jiraexport.export;

import java.util.List;
import java.util.Map;

public class MergeInputFile {
    private final List<Map<String, String>> rows;

    public MergeInputFile(List<Map<String, String>> rows) {
        this.rows = rows;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }
}
