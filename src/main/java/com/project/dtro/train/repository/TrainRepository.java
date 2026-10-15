package com.project.dtro.train.repository;

import com.project.dtro.train.domain.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long>, JpaSpecificationExecutor<Train> {
}
