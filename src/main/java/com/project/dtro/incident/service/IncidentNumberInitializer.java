package com.project.dtro.incident.service;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@RequiredArgsConstructor
public class IncidentNumberInitializer implements ApplicationRunner {
    private final IncidentRepository incidents;
    private final NumberGenerator numberGenerator;

    // 기존 개발 데이터에 장애번호를 채우고 등록·수정 정보는 유지한다.
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (var incident : incidents.findByIncidentNumberIsNullOrderByIdAsc()) {
            incidents.setIncidentNumber(incident.getId(), numberGenerator.nextNumber("INC", 5));
        }
    }
}
