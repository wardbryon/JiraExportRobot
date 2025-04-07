package com.sunnyset.jira.jiraexport.rest;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class JiraRestClientConfiguration {
    @Value("${jira.api.token}")
    public String jiraApiToken;
    @Value("${jira.api.url}")
    private String jiraApiUrl;
    @Value("${jira.api.user}")
    private String jiraApiUser;

    @Bean
    public JiraRestClient jiraRestClient() {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = URI.create(jiraApiUrl);
        return factory.createWithAuthenticationHandler(jiraServerUri, builder -> builder.setHeader("Authorization", "Basic " + Base64.encodeBase64String((jiraApiUser + ":" + jiraApiToken).getBytes())));
    }
}
