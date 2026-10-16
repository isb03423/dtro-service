package com.project.dtro.user.repository;

import com.project.dtro.user.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserid(String userid);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE users SET created_by = id WHERE userid IN (:userids) AND created_by IS DISTINCT FROM id", nativeQuery = true)
    int setOwnCreatorForDevAccounts(List<String> userids);
}
