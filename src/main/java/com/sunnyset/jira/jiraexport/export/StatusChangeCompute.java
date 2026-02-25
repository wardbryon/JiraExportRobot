package com.sunnyset.jira.jiraexport.export;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import org.joda.time.DateTime;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StatusChangeCompute {

    private final List<ChangelogGroup> changelogGroups;

    public StatusChangeCompute(Iterable<ChangelogGroup> changelogGroups) {
        this.changelogGroups = StreamSupport.stream(changelogGroups.spliterator(), false)
                .collect(Collectors.toList());
    }

    List<StatusChange> extractStatusChanges() {
        List<StatusChange> statusChanges = new ArrayList<>();

        for (ChangelogGroup group : changelogGroups) {
            for (ChangelogItem item : group.getItems()) {
                if ("status".equalsIgnoreCase(item.getField())) {
                    statusChanges.add(new StatusChange(
                            item.getFromString(),
                            item.getToString(),
                            group.getCreated()
                    ));
                }
            }
        }
        statusChanges.sort(Comparator.comparing(sc -> sc.changedAt));
        return statusChanges;
    }

    List<StatusDuration> computeStatusDurations(List<StatusChange> changes, DateTime created) {
        List<StatusDuration> result = new ArrayList<>();

        ZonedDateTime prevTime = asZonedDateTime(created.toDate());
        String prevStatus = "";

        for (StatusChange change : changes) {
            result.add(new StatusDuration(change.fromStatus, Duration.between(prevTime, change.changedAt)));
            prevTime = change.changedAt;
            prevStatus = change.toStatus;
        }

        result.add(new StatusDuration(prevStatus, Duration.between(prevTime, ZonedDateTime.now())));

        return result;
    }

    private static ZonedDateTime asZonedDateTime(Date created) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime prevTime = created.toInstant().atZone(zone);
        return prevTime;
    }

    String formatDuration(Duration d) {
        if(d == null) return "";
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        long minutes = d.minusDays(days).minusHours(hours).toMinutes();
        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }

    String formatDurationAsDays(Duration d) {
        if(d == null) return "";
        long days = d.toDays();
        return String.format("%d", days);
    }

    public String timeInStatus(String column) {
        Map<String, Duration> aggregated = extractDurations();
        return formatDuration(aggregated.get(column));
    }

    public String daysInStatus(String column) {
        Map<String, Duration> aggregated = extractDurations();
        return formatDurationAsDays(aggregated.get(column));
    }

    private Map<String, Duration> extractDurations() {
        List<StatusDuration> statusDurations = computeStatusDurations(extractStatusChanges(), created());
        Map<String, Duration> aggregated = statusDurations.stream()
                .collect(Collectors.groupingBy(
                        sd -> sd.status,
                        Collectors.reducing(
                                Duration.ZERO,
                                sd -> sd.duration,
                                Duration::plus
                        )
                ));
        return aggregated;
    }

    private DateTime created() {
        if(changelogGroups.isEmpty()) return new DateTime();
        return changelogGroups.get(changelogGroups.size()-1).getCreated();
    }

    class StatusChange {
        String fromStatus;
        String toStatus;
        ZonedDateTime changedAt;

        public StatusChange(String fromStatus, String toStatus, DateTime changedAt) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
            this.changedAt = asZonedDateTime(changedAt.toDate());
        }
    }

    class StatusDuration {
        String status;
        Duration duration;

        public StatusDuration(String status, Duration duration) {
            this.status = status;
            this.duration = duration;
        }
    }
}
