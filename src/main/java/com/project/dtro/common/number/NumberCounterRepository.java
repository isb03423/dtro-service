package com.project.dtro.common.number;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NumberCounterRepository extends JpaRepository<NumberCounter, String> {
    // 같은 종류의 번호를 동시에 요청해도 순번이 겹치지 않도록 해당 행을 잠근다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select counter from NumberCounter counter where counter.prefix = :prefix")
    Optional<NumberCounter> findForUpdate(@Param("prefix") String prefix);
}
