package com.sunnyset.csv.excelimport;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvToExcelImporterTest {

    @Test
    void testImportCsvToExcelLastValueOfEmptyStringIsIgnored() {
        String headerToSplit = "Key^Parent^ETRM- HLE^Octopus- HLE^Lumipower- HLE^KPI^Issue Type^Summary^Status^Assignee^Sprint";
        String lineToSplit = "LUMI-9644^LUMI-5783^^^^9,0^Feature^[CANCELLED] ITOT: Setup Market MRS and Fetch Elia Open Data publications^Cancelled^matthijs.lantmeeters.ext@luminus.be^";
        String[] header = headerToSplit.split(Pattern.quote("^"), -1);
        String[] values = lineToSplit.split(Pattern.quote("^"),-1);
        assertTrue(header.length == values.length, "Header and values should have the same length");
    }

}