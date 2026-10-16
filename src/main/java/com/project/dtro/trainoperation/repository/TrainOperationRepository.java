package com.project.dtro.trainoperation.repository;

import com.project.dtro.trainoperation.domain.TrainOperation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainOperationRepository extends JpaRepository<TrainOperation, Long>, JpaSpecificationExecutor<TrainOperation> {
    List<TrainOperation> findByOperationNumberIsNullOrderByIdAsc();
    List<TrainOperation> findByOperationNumberStartingWith(String prefix);

    @Modifying
    @Query(value = "UPDATE train_operation SET operation_number = :number WHERE operation_id = :id", nativeQuery = true)
    void setOperationNumber(@Param("id") Long id, @Param("number") String number);
}
