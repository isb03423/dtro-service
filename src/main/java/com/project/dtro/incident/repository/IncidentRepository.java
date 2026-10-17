package com.project.dtro.incident.repository;

import com.project.dtro.incident.domain.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    List<Incident> findByIncidentNumberIsNullOrderByIdAsc();

    @Modifying
    @Query(value = "UPDATE incident SET incident_number = :number WHERE incident_id = :id", nativeQuery = true)
    void setIncidentNumber(@Param("id") Long id, @Param("number") String number);

    @Override
    @EntityGraph(attributePaths = {"operation", "operation.train", "createdBy", "updatedBy"})
    Page<Incident> findAll(Specification<Incident> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"operation", "operation.train", "createdBy", "updatedBy"})
    Optional<Incident> findById(Long id);
}
