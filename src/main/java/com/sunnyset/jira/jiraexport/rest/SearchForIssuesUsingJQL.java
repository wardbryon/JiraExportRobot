package com.sunnyset.jira.jiraexport.rest;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchForIssuesUsingJQL {
    private static final Logger logger = LoggerFactory.getLogger(SearchForIssuesUsingJQL.class);
    public static final int MAX_RESULTS_DEFINED_BY_JIRA_API = 100;
    @Autowired
    private JiraRestClient jiraRestClient;
    @Value("${jira.jql}")
    public String jiraQuery;

    public List<Issue> findAll() {
        List<Issue> result = new ArrayList<>();
        try {
            int startAt = 0;
            Promise<SearchResult> searchResultPromise =
                    jiraRestClient.getSearchClient()
                                        .searchJql(jiraQuery, MAX_RESULTS_DEFINED_BY_JIRA_API, startAt,null);
            SearchResult searchResult = searchResultPromise.claim();
            int total = searchResult.getTotal();
            logger.info("Total of {} entries found", total);
            while(startAt < total) {
                logger.info("Retrieving entries, percentage done: {}%", (startAt * 100) / total);
                searchResultPromise =
                        jiraRestClient.getSearchClient()
                                .searchJql(jiraQuery, MAX_RESULTS_DEFINED_BY_JIRA_API, startAt,null);
                searchResult = searchResultPromise.claim();
                searchResult.getIssues().forEach(result::add);
                startAt += MAX_RESULTS_DEFINED_BY_JIRA_API;
            }
        } catch(Exception e) {
            logger.error("Error searching for result", e);
            throw new RuntimeException(e);
        }
        return result;
    }

}
