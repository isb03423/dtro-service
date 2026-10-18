package com.project.dtro.station;

import com.project.dtro.station.domain.Station;
import com.project.dtro.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Order(2)
@RequiredArgsConstructor
public class StationDataInitializer implements ApplicationRunner {
    private final StationRepository stations;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> existingCodes = new HashSet<>();
        stations.findAll().forEach(station -> existingCodes.add(station.getStationCode()));

        Map<String, Integer> orderByLine = new HashMap<>();
        List<Station> missing = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("stations.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String row;
            while ((row = reader.readLine()) != null) {
                if (row.isBlank()) continue;
                String[] values = row.split(",", -1);
                if (values.length != 3 || values[0].isBlank() || values[1].isBlank() || values[2].isBlank())
                    throw new IllegalStateException("역 CSV 형식이 올바르지 않습니다: " + row);

                String lineCode = values[0].trim();
                String stationCode = values[1].trim();
                String stationName = values[2].trim();
                int stationOrder = orderByLine.merge(lineCode, 1, Integer::sum);
                if (!existingCodes.add(stationCode)) continue;

                missing.add(Station.builder()
                        .lineCode(lineCode)
                        .stationCode(stationCode)
                        .stationName(stationName)
                        .stationOrder(stationOrder)
                        .build());
            }
        } catch (IOException cause) {
            throw new UncheckedIOException("역 CSV를 읽을 수 없습니다.", cause);
        }

        if (!missing.isEmpty()) stations.saveAll(missing);
    }
}
