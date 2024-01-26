package com.cegeka.everesst.jiraexport;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.cegeka.everesst.jiraexport.export.ExportWriter;
import com.cegeka.everesst.jiraexport.rest.SearchForIssuesUsingJQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.cegeka.everesst.jiraexport.*")
@EnableConfigurationProperties
public class JiraExportApplication implements CommandLineRunner {

    @Autowired
    private SearchForIssuesUsingJQL issueFieldsGetFields;
    @Autowired
    private ExportWriter exportWriter;

    public static void main(String[] args) {
        new SpringApplicationBuilder(JiraExportApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        List<Issue> queryResult = issueFieldsGetFields.findAll();
        exportWriter.writeToFileSystem(queryResult);
    }
}
