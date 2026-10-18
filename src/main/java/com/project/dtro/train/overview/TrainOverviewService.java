package com.project.dtro.train.overview;

import com.project.dtro.incident.domain.IncidentStatus;
import com.project.dtro.incident.repository.IncidentRepository;
import com.project.dtro.train.domain.TrainStatus;
import com.project.dtro.train.repository.TrainRepository;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;

@Service
@RequiredArgsConstructor
public class TrainOverviewService {
    private final TrainRepository trains;
    private final TrainOperationRepository operations;
    private final IncidentRepository incidents;

    @Transactional(readOnly = true)
    public TrainOverviewResponse getOverview() {
        var trainCounts = new EnumMap<TrainStatus, Long>(TrainStatus.class);
        for (TrainStatus status : TrainStatus.values()) {
            trainCounts.put(status, trains.countByStatus(status));
        }

        var incidentCounts = new EnumMap<IncidentStatus, Long>(IncidentStatus.class);
        for (IncidentStatus status : IncidentStatus.values()) {
            incidentCounts.put(status, incidents.countByStatus(status));
        }

        var recent = incidents.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "occurredAt", "id")))
                .getContent().stream()
                .map(incident -> new TrainOverviewResponse.RecentIncident(
                        incident.getId(),
                        incident.getIncidentNumber(),
                        incident.getOperation().getTrain().getTrainNumber(),
                        incident.getIncidentType(),
                        incident.getStatus(),
                        incident.getOccurredAt()))
                .toList();

        long trainTotal = trainCounts.values().stream().mapToLong(Long::longValue).sum();
        long incidentTotal = incidentCounts.values().stream().mapToLong(Long::longValue).sum();
        return new TrainOverviewResponse(trainTotal, trainCounts, operations.count(), incidentTotal,
                incidentCounts, recent, OffsetDateTime.now());
    }
}
