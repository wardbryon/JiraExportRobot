# JiraExportRobot
Robot to export Jira tickets, since the Jira Cloud only allows exporting 1000 entries

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

Beware of jira.filter.pages, because of a Jira limitation, in order to do paging, the PRJ-xxx must actually exist in the project (not been deleted).
That's why it must be an extensive list.