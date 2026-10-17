package com.project.dtro.trainoperation.service;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Order(2)
@RequiredArgsConstructor
public class TrainOperationNumberInitializer implements ApplicationRunner {
    private final TrainOperationRepository operations;
    private final NumberGenerator numberGenerator;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (var operation : operations.findByOperationNumberIsNullOrderByIdAsc()) {
            operations.setOperationNumber(operation.getId(), numberGenerator.nextNumber("OPS", 5));
        }
        for (var operation : operations.findByOperationNumberStartingWith("OPS-")) {
            String number = operation.getOperationNumber();
            if (number.matches("OPS-\\d{1,4}")) {
                operations.setOperationNumber(operation.getId(), String.format(Locale.ROOT, "OPS-%05d",
                        Integer.parseInt(number.substring(4))));
            }
        }
    }
}
