package com.sunnyset.csv.excelimport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Component
public class CsvToExcelImporter {
    private static final Logger logger = LoggerFactory.getLogger(CsvToExcelImporter.class);
    public static final int EMPTY_STRING_AS_LAST_SPLIT_VALUE = -1;

    @Value("${csv.separator}")
    private String csvSeparator;
    @Value("${csv.format.number.separator}")
    private String numberSeparator;
    @Value("${excel.file.input}")
    private String excelInputFile;
    @Value("${excel.file.output}")
    private String excelOutputFile;
    @Value("${csv.sheet.mappings}")
    private String csvSheetMappings;

    record CsvSheetMapping(String csvPath, int skipRowInput, String sheetName, int skipRowOutput) {
    }

    public void importCsvToExcel() {
        try {
            List<CsvSheetMapping> mappings = parseMappings(csvSheetMappings);
            importCsvToExcelInternal(mappings);
        } catch (Exception e) {
            logger.error("Error importing CSV to Excel: ", e);
        }
    }

    private List<CsvSheetMapping> parseMappings(String csvSheetMappings) {
        return Arrays.stream(csvSheetMappings.split(Pattern.quote(" | "), EMPTY_STRING_AS_LAST_SPLIT_VALUE))
                .map(mapping -> {
                    String[] parts = mapping.split(",", EMPTY_STRING_AS_LAST_SPLIT_VALUE);
                    if (parts.length != 4) {
                        throw new IllegalArgumentException("Invalid mapping format: " + mapping);
                    }
                    return new CsvSheetMapping(
                            parts[0].trim(),
                            Integer.parseInt(parts[1].trim()),
                            parts[2].trim(),
                            Integer.parseInt(parts[3].trim())
                    );
                })
                .toList();
    }

    private void importCsvToExcelInternal(List<CsvSheetMapping> mappings) throws Exception {
        FileInputStream fis = new FileInputStream(excelInputFile);
        Workbook workbook = new XSSFWorkbook(fis);
        fis.close();

        for (CsvSheetMapping mapping : mappings) {
            insertCsvIntoWorkbookSheet(mapping, workbook);
        }

        workbook.setForceFormulaRecalculation(true);

        FileOutputStream fos = new FileOutputStream(excelOutputFile);
        workbook.write(fos);
        workbook.close();
        fos.close();

        logger.info("CSV files successfully added to " + excelOutputFile);
    }

    private void insertCsvIntoWorkbookSheet(CsvSheetMapping mapping, Workbook workbook) throws IOException {
        Sheet sheet = workbook.getSheet(mapping.sheetName);

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + mapping.sheetName);
        }

        AtomicInteger outputRowNum = new AtomicInteger(mapping.skipRowOutput);
        Files.readAllLines(Paths.get(mapping.csvPath)).stream()
                .skip(mapping.skipRowInput)
                .forEach(line -> processCsvLine(line, sheet, outputRowNum));
    }

    private void processCsvLine(String line, Sheet sheet, AtomicInteger outputRowNum) {
        String[] values = line.split(Pattern.quote(csvSeparator), EMPTY_STRING_AS_LAST_SPLIT_VALUE);
        Row row = sheet.getRow(outputRowNum.get());
        if (row == null) row = sheet.createRow(outputRowNum.get());

        for (int col = 0; col < values.length; col++) {
            String value = values[col].trim();
            Cell cell = row.getCell(col);
            if (cell == null) cell = row.createCell(col);

            if (isANumber(value)) {
                cell.setCellValue(Double.parseDouble(value.replace(numberSeparator, ".")));
            } else {
                cell.setCellValue(value);
            }

        }
        outputRowNum.getAndIncrement();
    }

    private boolean isANumber(String value) {
        String pattern = "-?\\d+(" + Pattern.quote(numberSeparator) + "\\d+)?";
        return value.matches(pattern);
    }

}
