package com.project.dtro.incident.repository;

import com.project.dtro.incident.domain.IncidentStatusHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistory, Long> {
    @EntityGraph(attributePaths = "changedBy")
    List<IncidentStatusHistory> findByIncidentIdOrderByChangedAtDescIdDesc(Long incidentId);

    void deleteByIncidentId(Long incidentId);
}
