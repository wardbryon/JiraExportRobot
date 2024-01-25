# JiraExportRobot
Robot to export Jira tickets, since the Jira Cloud only allows exporting 1000 entries
Since Jira Cloud is not allowing to change the CSV delimiter, the only option is to export as HTML and parse it.

## Run with
VM Option :
    -Dspring.config.additional-location=file:credentials.properties
containing the user's credentials and url's
(in IntelliJ, you have to add this in the Run Configuration as VM Option, this is not always visible in the dialog)

## Parameters
To configure in credentials.properties
jira.login.url=
jira.login.mail.address=
jira.filter.url=
jira.filter=project = ... order by created DESC
jira.key.prefix=PRJ
jira.filter.pages=1,1001,2001,3001
download.location=
export.columns=Key,Issue Type,Summary,EverESSt Domain,Epic Link Key,Story Points,Status,Sprint,EverESSt Team,Fix versions
export.columns.treatment=RAW,RAW,RAW,RAW,RAW,RAW,RAW,LAST_ENTRY_ALPHABETICAL_SORT,RAW,ONE_ENTRY_VALIDATION

Beware of jira.filter.pages, because of a Jira limitation, in order to do paging, the PRJ-xxx must actually exist in the project (not been deleted).
That's why it must be an extensive list.

The export columns are the columns that will be exported in the CSV file.
The treatment is the way the column will be treated,
either RAW (no treatment), 
FIRST_ENTRY (only the first entry will be kept), 
ONE_ENTRY_VALIDATION (all entries will be kept but an error will be logged)
LAST_ENTRY_ALPHABETICAL_SORT (after sorting alphabetically, the last entry will be kept, this is mainly useful for the Sprint column)
For this it is important that the next chronological sprint is alphabetically behind the previous one.