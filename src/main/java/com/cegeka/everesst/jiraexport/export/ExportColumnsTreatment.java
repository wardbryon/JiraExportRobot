package com.cegeka.everesst.jiraexport.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ExportColumnsTreatment {

        RAW {
            @Override
            public String treat(String value) {
                return value;
            }
        },
        LAST_ENTRY {
            @Override
            public String treat(String value) {
                return value.split(",")[value.split(",").length - 1];
            }
        },
        ONE_ENTRY_VALIDATION {
            private static final Logger logger = LoggerFactory.getLogger(ExportColumnsTreatment.class);
            @Override
            public String treat(String value) {
                if(value.split(",").length > 1){
                    logger.error("More than one entry found, while only one was expected {}", value);
                }
                return value;
            }
        };

        public abstract String treat(String value);


}
