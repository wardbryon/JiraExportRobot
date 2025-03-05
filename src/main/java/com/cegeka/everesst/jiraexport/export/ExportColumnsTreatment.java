package com.cegeka.everesst.jiraexport.export;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Version;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;

public enum ExportColumnsTreatment {

        CUSTOM_FIELD_STRING {
            @Override
            public String treat(Issue issue, String column) throws Exception {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Object value = issue.getFieldByName(column).getValue();
                if(value instanceof String){
                    return (String) value;
                }else if(value instanceof JSONObject){
                    JSONObject jsonObject = (JSONObject) value;
                    if(!jsonObject.has("value")){
                        return "";
                    }
                    return jsonObject.getString("value");
                }
                return "";
            }
        },
        CUSTOM_FIELD_DOUBLE_COMMA {
            @Override
            public String treat(Issue issue, String column) {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Double doubleValue = (Double) field.getValue();
                if( doubleValue == null){
                    return "";
                }
                return String.valueOf(doubleValue).replace(".", ",");
            }
        },
        CUSTOM_FIELD_DOUBLE {
            @Override
            public String treat(Issue issue, String column) {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Double doubleValue = (Double) field.getValue();
                if( doubleValue == null){
                    return "";
                }
                return String.valueOf(doubleValue);
            }
        },

        KEY {
            @Override
            public String treat(Issue value, String column) {
                return value.getKey();
            }
        },
        STATUS {
            @Override
            public String treat(Issue value, String column) {
                return value.getStatus().getName();
            }
        },
        SUMMARY {
            @Override
            public String treat(Issue value, String column) {
                return value.getSummary();
            }
        },
        ASSIGNEE {
            @Override
            public String treat(Issue value, String column) {
                if(value.getAssignee() == null){
                    return "";
                }
                return value.getAssignee().getEmailAddress();
            }
        },
        ISSUE_TYPE {
            @Override
            public String treat(Issue value, String column) {
                return value.getIssueType().getName();
            }
        },
        CUSTOM_FIELD_LAST_ENTRY_ALPHABETICAL_SORT{
            @Override
            public String treat(Issue issue, String column) throws Exception  {
                JSONArray jsonArray = (JSONArray) issue.getFieldByName(column).getValue();
                if(jsonArray == null || jsonArray.length() == 0){
                    return "";
                }
                Map<Long,String> map = new HashMap<>();
                for (int i =0; i < jsonArray.length(); i++) {
                    map.put(jsonArray.getJSONObject(i).getLong("id"), jsonArray.getJSONObject(i).getString("name"));
                }
                return map.get(map.keySet().stream().sorted().toList().get(map.keySet().size()-1));
            }
        },
        FIX_VERSION_ONE_ENTRY_VALIDATION {
            private static final Logger logger = LoggerFactory.getLogger(ExportColumnsTreatment.class);
            @Override
            public String treat(Issue issue, String column) {
                List<Version> versionsList = stream(issue.getFixVersions().spliterator(), false)
                        .sorted(Comparator.comparing(Version::getId))
                        .toList();
                if(versionsList.size() > 1){
                    logger.error("More than one entry found, while only one was expected {}", versionsList);
                }
                return stream(issue.getFixVersions().spliterator(), false)
                        .sorted(Comparator.comparing(Version::getId))
                        .map(Version::getName).findFirst().orElse("");
            }
        };

        public abstract String treat(Issue issue, String column) throws Exception;


}
