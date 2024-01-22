package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;

public enum ExportColumnsTreatment {

        RAW {
            @Override
            public String treat(String value) {
                return value.trim();
            }
        },
        LAST_ENTRY {
            @Override
            public String treat(String value) {
                return value.split(",")[value.split(",").length - 1].trim();
            }
        },

        LAST_ENTRY_ALPHABETICAL_SORT{
            @Override
            public String treat(String value) {
                return Arrays.stream(value.split(",")).map(String::trim).sorted(Comparator.reverseOrder()).toList().get(0);
            }
        },

        FIRST_ENTRY {
            @Override
            public String treat(String value) {
                return value.split(",")[0].trim();
            }
        },
        ONE_ENTRY_VALIDATION {
            private static final Logger logger = LoggerFactory.getLogger(ExportColumnsTreatment.class);
            @Override
            public String treat(String value) {
                if(value.split(",").length > 1){
                    logger.error("More than one entry found, while only one was expected {}", value);
                }
                return value.trim();
            }
        };

        public abstract String treat(String value);


}
