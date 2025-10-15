package com.sunnyset.file.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;

@Component
public class BackupFileRenamer {
    private static final Logger logger = LoggerFactory.getLogger(BackupFileRenamer.class);

    @Value("${files.to.timestamp}")
    private String filesToTimestamp;

    @Value("${files.to.rename.in.sequence}")
    private String filesToRenameInSequence;

    private static final java.util.regex.Pattern RENAME_PAIR_PATTERN = java.util.regex.Pattern.compile("\\(([^,]+),([^)]+)\\)");


    public void timestampFilesThenRename() {
        timestampFiles();
        renameFilesInSequence();
    }

    private void renameFilesInSequence() {
        if (filesToRenameInSequence == null || filesToRenameInSequence.trim().isEmpty()) {
            logger.warn("No files to rename in sequence specified.");
            return;
        }
        Matcher matcher = RENAME_PAIR_PATTERN.matcher(filesToRenameInSequence.trim());
        if (!matcher.find()) {
            logger.error("No valid (source,target) pairs found in property: {}", filesToRenameInSequence.trim());
            return;
        }
        do {
            File source = new File(matcher.group(1).trim());
            File destination = new File(matcher.group(2).trim());
            if (!source.exists()) {
                logger.error("Source file does not exist: {}. Stopping sequence.", source.getAbsolutePath());
                return;
            }
            if (destination.exists()) {
                if (!destination.delete()) {
                    logger.error("Destination file already exists and could not be deleted: {}. Stopping sequence.", destination.getAbsolutePath());
                    return;
                } else {
                    logger.info("Destination file already exists and will be overwritten: {}", destination.getAbsolutePath());
                }
            }
            boolean success = source.renameTo(destination);
            if (success) {
                logger.info("Renamed {} to {}", source.getAbsolutePath(), destination.getAbsolutePath());
            } else {
                logger.error("Failed to rename {} to {}. Stopping sequence.", source.getAbsolutePath(), destination.getAbsolutePath());
                return;
            }
        } while (matcher.find());
    }

    private void timestampFiles() {
        Arrays.stream(filesToTimestamp.split(",")).forEach(filePath -> {
            File file = new File(filePath.trim());
            if (file.exists()) {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String newName = timestamp + " " + file.getName();
                File dest = new File(file.getParent(), newName);
                try {
                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Copied {} to {}", file.getAbsolutePath(), dest.getAbsolutePath());
                } catch (Exception e) {
                    logger.error("Failed to copy file: {}", file.getAbsolutePath(), e);
                }
            } else {
                logger.warn("File does not exist: {}", file.getAbsolutePath());
            }
        });
    }
}
