package com.project.dtro.train.repository;

import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long>, JpaSpecificationExecutor<Train> {
    long countByStatus(TrainStatus status);

    boolean existsByFormationName(String formationName);
}
