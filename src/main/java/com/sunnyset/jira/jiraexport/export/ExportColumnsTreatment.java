package com.sunnyset.jira.jiraexport.export;

import com.atlassian.jira.rest.client.api.domain.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public enum ExportColumnsTreatment {

     CUSTOM_FIELD_STRING {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Object value = issue.getFieldByName(column).getValue();
                if(value instanceof String){
                    return (String) value;
                }else if(value instanceof JSONObject jsonObject){
                    if (jsonObject.has("value")) {
                        return jsonObject.getString("value");
                    }
                    return "";
                }else if (value instanceof JSONArray jsonArray){
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        stringBuilder.append(jsonArray.getJSONObject(i).getString("value"));
                        if (i != jsonArray.length() - 1) {
                            stringBuilder.append(exportConfig.numberSeparator());
                        }
                    }
                    return stringBuilder.toString();
                }
                return "";
            }
        },
        CUSTOM_FIELD_DOUBLE {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception{
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Object value = issue.getFieldByName(column).getValue();
                if(value instanceof Number){
                    return String.valueOf(value).replace(".", exportConfig.numberSeparator());
                }else if(value instanceof JSONObject jsonObject){
                    if (jsonObject.has("value")) {
                        return String.valueOf(jsonObject.getDouble("value")).replace(".", exportConfig.numberSeparator());
                    }
                    return "";
                }
                return "";
            }
        },
        KEY {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                return value.getKey();
            }
        },
        STATUS {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                return value.getStatus().getName();
            }
        },
        SUMMARY {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                return value.getSummary();
            }
        },
        ASSIGNEE {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                if(value.getAssignee() == null){
                    return "";
                }
                return value.getAssignee().getEmailAddress();
            }
        },
        REPORTER {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                if(value.getAssignee() == null){
                    return "";
                }
                return value.getAssignee().getEmailAddress();
            }
        },
        LAST_SPRINT {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Object value = issue.getFieldByName(column).getValue();
                if(value instanceof JSONArray jsonArraySprint){
                    Map<Integer,String> map = new HashMap<>();
                    if(jsonArraySprint.length() == 0){
                        return "";
                    }
                    for (int i =0; i < jsonArraySprint.length(); i++) {
                        map.put(jsonArraySprint.getJSONObject(i).getInt("id"), jsonArraySprint.getJSONObject(i).getString("name"));
                    }
                    // Taking the highest id, which is the last sprint
                    OptionalInt max = map.keySet().stream().mapToInt(Integer::intValue).max();
                    return max.getAsInt() + " - " + map.get(max.getAsInt());
                }
                return "";
            }
        },
        PARENT_KEY {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception {
                RelativeTask parentTask = issue.getParentTask();
                return parentTask != null ? parentTask.getIssueKey() : "";
            }
        },
        LINKED_ISSUE_PARENT_OF {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                AtomicReference<String> returnValue = new AtomicReference<>("");
                issue.getIssueLinks().forEach(issueLink -> {
                    if(issueLink.getIssueLinkType().getName().equals("Parent/Child") && issueLink.getIssueLinkType().getDescription().equals("is the child of ")
                            && issueLink.getIssueLinkType().getDirection() == IssueLinkType.Direction.INBOUND){
                        returnValue.set(issueLink.getTargetIssueKey());
                    }
                });
                return returnValue.get();
            }
        },
        ISSUE_TYPE {
            @Override
            public String treat(Issue value, String column, ExportWriter.ExportConfig exportConfig) {
                return value.getIssueType().getName();
            }
        },
        CUSTOM_FIELD_DATE {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                IssueField field = issue.getFieldByName(column);
                if(field == null){
                    return "";
                }
                Object value = field.getValue();
                if(value == null){
                    return "";
                }else{
                    LocalDate date = LocalDate.parse(field.getValue().toString());
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(exportConfig.dateFormat());
                    return date.format(outputFormatter);
                }
            }
        },
        DUE_DATE {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                return DateTimeFormat.forPattern(exportConfig.dateFormat()).print(issue.getDueDate());
            }
        },
        LABELS {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                return issue.getLabels().stream().sorted().collect(Collectors.joining(exportConfig.listSeparator()));
            }
        },
        CUSTOM_FIELD_LAST_ENTRY_ALPHABETICAL_SORT{
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception  {
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
        STATUS_TIME {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                if(issue.getChangelog() == null || !issue.getChangelog().iterator().hasNext()){
                    LoggerFactory.getLogger(ExportColumnsTreatment.class).error("No change logs present");
                    return "";
                }
                return new StatusChangeCompute(issue.getChangelog()).timeInStatus(column);
            }
        },
        FIX_VERSIONS {
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
                return stream(issue.getFixVersions().spliterator(), false)
                        .sorted(Comparator.comparing(Version::getName))
                        .map(Version::getName)
                        .collect(Collectors.joining(exportConfig.listSeparator()));
            }
        },
        FIX_VERSION_ONE_ENTRY_VALIDATION {
            private static final Logger logger = LoggerFactory.getLogger(ExportColumnsTreatment.class);
            @Override
            public String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) {
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

        public abstract String treat(Issue issue, String column, ExportWriter.ExportConfig exportConfig) throws Exception;

}
