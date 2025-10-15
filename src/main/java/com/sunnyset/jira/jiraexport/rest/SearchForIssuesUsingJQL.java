package com.sunnyset.jira.jiraexport.rest;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TotalCount;
import io.atlassian.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SearchForIssuesUsingJQL {
    private static final Logger logger = LoggerFactory.getLogger(SearchForIssuesUsingJQL.class);
    @Autowired
    private JiraRestClient jiraRestClient;
    @Value("${jira.jql}")
    private String jiraQuery;
    @Value("${jira.changelogs}")
    private boolean includeChangeLogs;


    public List<Issue> findAll() {
        List<Issue> result = new ArrayList<>();
        try {
            Promise<TotalCount> totalCountPromise = jiraRestClient.getSearchClient().totalCount(jiraQuery);
            int total = totalCountPromise.claim().getCount();

            logger.info("Total of {} entries found", total);
            String nextPageToken = null;
            while (result.size() < total) {
                logger.info("Retrieving entries, percentage done: {}%", (result.size() * 100) / total);
                Promise<SearchResult> searchResultPromise =
                        jiraRestClient.getSearchClient()
                                .enhancedSearchJql(jiraQuery, null, nextPageToken, Set.of("*all"), null);
                SearchResult searchResult = searchResultPromise.claim();
                nextPageToken = searchResult.getNextPageToken();
                searchResult.getIssues().forEach(result::add);
            }

            if (includeChangeLogs) {
                logger.info("Including changelogs for each entry...");
                AtomicInteger counter = new AtomicInteger();
                return result.stream().map(
                        basicIssue -> {
                            counter.getAndIncrement();
                            if(counter.get()%10 == 0){
                                logger.info("Retrieving changelogs, percentage done: {}%", (counter.get() * 100) / total);
                            }
                            return jiraRestClient.getIssueClient().getIssue(
                                    basicIssue.getKey(), List.of(IssueRestClient.Expandos.CHANGELOG)
                            ).claim();
                        }).toList();
            }

        } catch (Exception e) {
            logger.error("Error searching for result", e);
            throw new RuntimeException(e);
        }
        return result;
    }

}
