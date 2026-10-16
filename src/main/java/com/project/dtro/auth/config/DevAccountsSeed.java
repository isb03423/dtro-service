package com.project.dtro.auth.config;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DevAccountsSeed {
    private static final Logger log = LoggerFactory.getLogger(DevAccountsSeed.class);
    private static final String DEV_PASSWORD = "demo1234";

    private final MemberRepository members;
    private final PasswordEncoder passwords;
    private final TransactionTemplate transactions;

    // 서버 시작 시 개발용 계정 3개를 준비한다.
    @Bean
    ApplicationRunner seedDevAccounts() {
        return args -> {
            List<SeedAccount> accounts = List.of(
                    new SeedAccount("admin_dev", "관리자", MemberRole.ADMIN),
                    new SeedAccount("train_manager_dev", "열차 운영자", MemberRole.TRAIN_MANAGER),
                    new SeedAccount("user_dev", "사용자", MemberRole.USER)
            );
            // 여러 계정 생성 작업을 하나의 트랜잭션으로 묶는다.
            transactions.executeWithoutResult(status -> {
                accounts.forEach(this::seed);
                // IDENTITY ID가 저장 후 발급되므로 세 계정의 생성자를 본인 ID로 연결한다.
                members.setOwnCreatorForDevAccounts(accounts.stream().map(SeedAccount::username).toList());
            });
        };
    }

    // 이미 설정이 맞는 계정은 다시 저장하지 않아 수정 이력을 만들지 않는다.
    private void seed(SeedAccount account) {
        var existing = members.findByUserid(account.username());
        if (existing.isPresent()) {
            Member member = existing.get();
            if (member.getName().equals(account.name())
                    && member.getRole() == account.role()
                    && member.isEnabled()
                    && passwords.matches(DEV_PASSWORD, member.getPassword())) {
                return;
            }
            member.updateDevAccount(passwords.encode(DEV_PASSWORD), account.name(), account.role());
            members.save(member);
            log.info("개발용 {} 계정을 갱신했습니다: {}", account.role(), account.username());
            return;
        }

        Member member = Member.builder()
                .userid(account.username())
                .password(passwords.encode(DEV_PASSWORD))
                .name(account.name())
                .role(account.role())
                .build();
        members.save(member);
        log.info("개발용 {} 계정을 생성했습니다: {}", account.role(), account.username());
    }

    private record SeedAccount(String username, String name, MemberRole role) {
    }
}
