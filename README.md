# Jira Export Tool
Tool to export Jira tickets, since the Jira Cloud UI only allows exporting 1000 entries.

Also provides a tool to insert the exported csv's into an existing excel file.

Additionally, it allows to automatically backup and rename the existing excel files for history purposes.

## How to run

### You need an Atlassian API token
Request it at
https://id.atlassian.com/manage-profile/security/api-tokens

### Run with
VM Option :
    -Dspring.config.additional-location=file:credentials.properties
containing the configuration

### Configuration of the Jira Export
More examples under config-samples folder.

#### Parameters
```
jira.api.url= https://<yourcompany>.atlassian.net
jira.api.user= email address
jira.api.token= your API token (see above)
jira.filter= JQL filter to use
export.columns= The columns to export
export.columns.names= The column headers
export.columns.treatment= How to treat the columns, this is the same order as the export.columns. Possible values are described below.
export.format.csv.seperator=^
export.format.number.seperator=,
export.format.date=dd/MM/yyyy
export.filename=File path to write the CSV file to
```
#### ExportColumnsTreatment possibilities
```
KEY : The Jira ticket key
ISSUE_TYPE : The type of the ticket, e.g. Story, Bug, Task
STATUS : The status of the ticket
SUMMARY : The summary of the ticket
FIX_VERSION_ONE_ENTRY_VALIDATION : The fix version of the ticket, only one entry will be kept, an error will be logged if more than 1 entry is found
CUSTOM_FIELD_STRING : A custom field of type string, works both with a single string value or an object with a value field
CUSTOM_FIELD_DOUBLE : A custom field of type double
CUSTOM_FIELD_LAST_ENTRY_ALPHABETICAL_SORT : A custom field with a list of strings, the last entry will be kept after sorting alphabetically
ASSIGNEE : The assignee of the ticket
LAST_SPRINT : The last sprint of the ticket
LINKED_ISSUE_PARENT_OF : The parent issue of the ticket, using the linked issues with the type "Child of"
PARENT_KEY : The parent key of the ticket
FIX_VERSIONS : All the fix versions of the ticket
```

### Configuration of the Excel Import
Example under config-samples folder.
```
excel.file.input=Target excel file path
excel.file.output=Output excel file path
csv.sheet.mappings=Mappings of csv's to Excel tabs, specifying rows to skip if necessary (1 is skip nothing). Seperated with |  exports/export-epics-company.csv,1,INPUT-PRJ-Epics,1
csv.separator=Csv separator, e.g. ^ or ;
csv.format.number.separator=,
```
