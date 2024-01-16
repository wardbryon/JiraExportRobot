# JiraExportRobot
Robot to export Jira tickets, since the Jira Cloud only allows exporting 1000 entries

## Run with
VM Option :

    -Dspring.config.additional-location=file:credentials.properties
containing the user's credentials and url's
(in IntelliJ, you have to add this in the Run Configuration as VM Option, this is not always visible in the dialog)