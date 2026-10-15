package com.project.dtro.auth.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DevAccountsSeed {
    private static final Logger log = LoggerFactory.getLogger(DevAccountsSeed.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final Environment environment;
    private final TransactionTemplate transactions;

    // 설정이 켜진 경우 개발용 계정을 생성하는 실행 작업을 등록한다.
    @Bean
    @ConditionalOnProperty(name = "dtro.dev.seed-enabled", havingValue = "true")
    ApplicationRunner seedTrainManager() {
        return args -> {
            String password = environment.getProperty("dtro.dev.seed-password");
            if (password == null || password.isBlank()) {
                throw new IllegalStateException("DTRO_DEV_SEED_PASSWORD 환경변수를 설정해야 합니다.");
            }

            List<SeedAccount> accounts = List.of(
                    new SeedAccount("admin_dev", "관리자", "ADMIN"),
                    new SeedAccount(environment.getProperty("dtro.dev.seed-username", "train_manager_dev"),
                            "열차 운영자", "TRAIN_MANAGER"),
                    new SeedAccount("user_dev", "사용자", "USER")
            );
            // 여러 계정 생성 작업을 하나의 트랜잭션으로 묶는다.
            transactions.executeWithoutResult(status -> accounts.forEach(account -> seed(account, password)));
        };
    }

    // 계정이 없을 때 비밀번호를 암호화해 개발용 사용자를 저장한다.
    private void seed(SeedAccount account, String password) {
        Integer count = jdbc.queryForObject(
                "select count(*) from users where userid = ?", Integer.class, account.username());
        if (count != null && count > 0) {
            log.info("개발용 계정이 이미 있습니다: {}", account.username());
            return;
        }

        Long id = jdbc.queryForObject("""
                insert into users (userid, password, name, role, enabled, created_at, created_by)
                values (?, ?, ?, ?, true, current_timestamp, 0)
                returning id
                """, Long.class, account.username(), passwords.encode(password), account.name(), account.role());
        // 생성 직후에는 자기 자신의 ID를 생성자로 기록한다.
        jdbc.update("update users set created_by = id where id = ?", id);
        log.info("개발용 {} 계정을 생성했습니다: {}", account.role(), account.username());
    }

    private record SeedAccount(String username, String name, String role) {
    }
}
