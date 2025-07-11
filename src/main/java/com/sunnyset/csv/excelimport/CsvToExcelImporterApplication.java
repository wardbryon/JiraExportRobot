package com.sunnyset.csv.excelimport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.sunnyset.csv.excelimport")
@EnableConfigurationProperties
public class CsvToExcelImporterApplication implements CommandLineRunner {

    @Autowired
    private CsvToExcelImporter csvToExcelImporter;

    public static void main(String[] args) {
        new SpringApplicationBuilder(CsvToExcelImporterApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        csvToExcelImporter.importCsvToExcel();
    }
}
