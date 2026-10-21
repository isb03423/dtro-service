package com.project.dtro.complaint.repository;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    long countByStatus(ComplaintStatus status);

    @Query("""
            SELECT c FROM Complaint c
            WHERE (:keyword IS NULL OR c.title = :keyword OR c.content = :keyword)
              AND (:status IS NULL OR c.status = :status)
              AND (:type IS NULL OR c.type = :type)
            """)
    Page<Complaint> search(
            @Param("keyword") String keyword,
            @Param("status") ComplaintStatus status,
            @Param("type") ComplaintType type,
            Pageable pageable
    );
}
