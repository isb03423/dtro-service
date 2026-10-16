package com.project.dtro.common.number;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NumberGenerator {
    public static final String LOCK_PREFIX = "SYSTEM_LOCK";
    private static final int DEFAULT_DIGITS = 3;

    private final NumberCounterRepository repository;

    // 자리 수를 생략하면 기본 3자리로 다음 번호를 발급한다.
    @Transactional
    public String nextNumber(String prefix) {
        return generate(prefix, DEFAULT_DIGITS);
    }

    // 전달한 자리 수로 다음 번호를 발급한다.
    @Transactional
    public String nextNumber(String prefix, int digits) {
        return generate(prefix, digits);
    }

    // 같은 번호 종류의 순번을 잠그고 요청한 자리 수에 맞춰 조합한다.
    private String generate(String prefix, int digits) {
        if (prefix == null || prefix.isBlank() || prefix.endsWith("-")
                || LOCK_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("번호 발급 규칙이 올바르지 않습니다.");
        }
        if (digits < 1) {
            throw new IllegalArgumentException("번호 자리 수가 올바르지 않습니다.");
        }

        NumberCounter counter = repository.findForUpdate(prefix).orElseGet(() -> createCounter(prefix));
        return prefix + "-"
                + String.format(Locale.ROOT, "%0" + digits + "d", counter.nextValue());
    }

    // 처음 쓰는 번호 종류는 공통 잠금 아래 카운터를 한 번만 만든다.
    private NumberCounter createCounter(String prefix) {
        repository.findForUpdate(LOCK_PREFIX)
                .orElseThrow(() -> new IllegalStateException("번호 발급기가 초기화되지 않았습니다."));
        return repository.findForUpdate(prefix)
                .orElseGet(() -> repository.saveAndFlush(new NumberCounter(prefix, 0)));
    }
}
