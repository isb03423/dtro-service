package com.project.dtro.common.number;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class NumberCounterInitializer implements ApplicationRunner {
    private final TransactionTemplate transactions;
    private final NumberCounterRepository repository;

    // 서버 시작 시 새 번호 종류를 등록할 때 사용할 공통 잠금 행을 준비한다.
    @Override
    public void run(ApplicationArguments args) {
        transactions.executeWithoutResult(status -> {
            if (!repository.existsById(NumberGenerator.LOCK_PREFIX)) {
                repository.saveAndFlush(new NumberCounter(NumberGenerator.LOCK_PREFIX, 0));
            }
        });
    }
}
