package com.project.dtro.train.overview;

import com.project.dtro.incident.domain.IncidentStatus;
import com.project.dtro.train.domain.TrainStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record TrainOverviewResponse(
        long trainTotal,
        Map<TrainStatus, Long> trainByStatus,
        long operationTotal,
        long incidentTotal,
        Map<IncidentStatus, Long> incidentByStatus,
        List<RecentIncident> recentIncidents,
        OffsetDateTime fetchedAt
) {
    public record RecentIncident(
            Long id,
            String incidentNumber,
            String trainNumber,
            String incidentType,
            IncidentStatus status,
            OffsetDateTime occurredAt
    ) {
    }
}
