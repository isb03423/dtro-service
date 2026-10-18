package com.project.dtro.user.repository;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    Optional<Member> findByUserid(String userid);

    boolean existsByUserid(String userid);

    long countByRole(MemberRole role);

    long countByEnabled(boolean enabled);

    List<Member> findTop5ByOrderByCreatedAtDescIdDesc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE users SET created_by = id WHERE userid IN (:userids) AND created_by IS DISTINCT FROM id", nativeQuery = true)
    int setOwnCreatorForDevAccounts(List<String> userids);
}
