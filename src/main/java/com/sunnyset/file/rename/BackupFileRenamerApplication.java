package com.sunnyset.file.rename;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.sunnyset.file.rename")
@EnableConfigurationProperties
public class BackupFileRenamerApplication implements CommandLineRunner {

    @Autowired
    private BackupFileRenamer backupFileRenamer;

    public static void main(String[] args) {
        new SpringApplicationBuilder(BackupFileRenamerApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        backupFileRenamer.timestampFilesThenRename();
    }
}
