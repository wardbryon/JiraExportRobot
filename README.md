# JiraExportRobot
Robot to export Jira tickets, since the Jira Cloud only allows exporting 1000 entries
Since Jira Cloud is not allowing to change the CSV delimiter, the only option is to export as HTML and parse it.

## Uses the jira-rest-java-client library
https://docs.atlassian.com/jira-rest-java-client-parent/5.0.4/apidocs/com/atlassian/jira/rest/client/api/SearchRestClient.html#searchJql(java.lang.String)


## Run with
VM Option :
    -Dspring.config.additional-location=file:credentials.properties
containing the configuration
(in IntelliJ, you have to add this in the Run Configuration as VM Option, this is not always visible in the dialog)

## Request an Atlassian API token
https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account/
https://id.atlassian.com/manage-profile/security/api-tokens

## Uses the following REST API's
To retrieve the fields to get the field id's
https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issue-fields/#api-rest-api-2-field-get
To retrieve the actual issues
https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issue-search/#api-rest-api-2-search-post

## Parameters
To configure in credentials.properties
jira.api.url= https://<yourcompany>.atlassian.net
jira.api.user= email address
jira.api.token= your API token (see above)
jira.filter= JQL filter to use
export.columns= The columns to export
export.columns.treatment= How to treat the columns, this is the same order as the export.columns. Possible values are described below.
export.csv.seperator=^
export.filename=File path to write the CSV file to

## ExportColumnsTreatment possibilities

KEY : The Jira ticket key
STATUS : The status of the ticket
SUMMARY : The summary of the ticket
FIX_VERSION_ONE_ENTRY_VALIDATION : The fix version of the ticket, only one entry will be kept, an error will be logged if more than 1 entry is found
CUSTOM_FIELD_STRING : A custom field of type string, works both with a single string value or an object with a value field
CUSTOM_FIELD_DOUBLE : A custom field of type double
CUSTOM_FIELD_LAST_ENTRY_ALPHABETICAL_SORT : A custom field with a list of strings, the last entry will be kept after sorting alphabetically