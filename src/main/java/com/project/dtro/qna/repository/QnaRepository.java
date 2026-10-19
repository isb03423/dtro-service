package com.project.dtro.qna.repository;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    @Query("""
            SELECT c FROM Qna c
            WHERE (:keyword IS NULL OR c.title = :keyword OR c.content = :keyword)
              AND (:status IS NULL OR c.status = :status)
              AND (:type IS NULL OR c.type = :type)
            """)
    // 민원과 동일하게 제목·내용 전체 일치와 유형·상태 조건으로 검색한다.
    Page<Qna> search(
            @Param("keyword") String keyword,
            @Param("status") QnaStatus status,
            @Param("type") QnaType type,
            Pageable pageable
    );
}
