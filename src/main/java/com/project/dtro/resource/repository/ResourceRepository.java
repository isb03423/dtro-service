package com.project.dtro.resource.repository;

import com.project.dtro.resource.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM Resource r WHERE " +
            "(:publicYn IS NULL OR r.publicYn = :publicYn) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "(:searchType = 'title' AND r.title LIKE %:keyword%) OR " +
            "(:searchType = 'content' AND r.content LIKE %:keyword%) OR " +
            "(:searchType = 'all' AND (r.title LIKE %:keyword% OR r.content LIKE %:keyword%)))")
    Page<Resource> findByFilters(@Param("publicYn") Boolean publicYn,
                                 @Param("keyword") String keyword,
                                 @Param("searchType") String searchType,
                                 Pageable pageable);
}
