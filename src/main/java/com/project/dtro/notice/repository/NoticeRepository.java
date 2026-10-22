package com.project.dtro.notice.repository;

import com.project.dtro.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n WHERE " +
            "(:publicYn IS NULL OR n.publicYn = :publicYn) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "(:searchType = 'title' AND n.title LIKE %:keyword%) OR " +
            "(:searchType = 'content' AND n.content LIKE %:keyword%) OR " +
            "(:searchType = 'all' AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)))")
    Page<Notice> findByFilters(@Param("publicYn") Boolean publicYn,
                               @Param("keyword") String keyword,
                               @Param("searchType") String searchType,
                               Pageable pageable);
}
