package com.project.dtro.train.overview;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.incident.domain.Incident;
import com.project.dtro.incident.domain.IncidentStatus;
import com.project.dtro.incident.domain.IncidentStatusHistory;
import com.project.dtro.incident.repository.IncidentRepository;
import com.project.dtro.incident.repository.IncidentStatusHistoryRepository;
import com.project.dtro.station.domain.Station;
import com.project.dtro.station.repository.StationRepository;
import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import com.project.dtro.train.repository.TrainRepository;
import com.project.dtro.trainoperation.domain.TrainOperation;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import com.project.dtro.user.domain.Member;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Order(3)
@ConditionalOnProperty(prefix = "dtro.demo-data", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class TrainOverviewDemoDataSeed implements ApplicationRunner {
    private static final String FIRST_DEMO_TRAIN = "현황 데모 01편성";
    private static final String[] INCIDENT_TYPES = {"신호 이상", "차량 이상", "출입문 이상", "운행 지연"};

    private final TrainRepository trains;
    private final TrainOperationRepository operations;
    private final IncidentRepository incidents;
    private final IncidentStatusHistoryRepository histories;
    private final StationRepository stations;
    private final MemberRepository members;
    private final NumberGenerator numbers;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (trains.existsByFormationName(FIRST_DEMO_TRAIN)) {
            return;
        }

        Member actor = members.findByUserid("admin_dev")
                .orElseThrow(() -> new IllegalStateException("현황 데모 데이터에 사용할 개발 관리자 계정이 없습니다."));
        List<List<Station>> lines = stations.findLineCodes().stream()
                .map(stations::findByLineCodeOrderByStationOrderAsc)
                .filter(line -> line.size() >= 8)
                .toList();
        if (lines.isEmpty()) {
            throw new IllegalStateException("현황 데모 데이터에 사용할 역 정보가 부족합니다.");
        }

        LocalDate today = LocalDate.now();
        List<Train> demoTrains = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Train train = new Train(String.format(Locale.ROOT, "현황 데모 %02d편성", i + 1),
                    "도시철도 전동차", i % 4 == 0 ? 8 : 6, TrainStatus.values()[i % 3],
                    today.minusYears(3 + i % 13), today.minusDays(1 + i % 35));
            train.assignTrainNumber(numbers.nextNumber("TRN", 4));
            demoTrains.add(trains.save(train));
        }

        List<TrainOperation> demoOperations = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            List<Station> line = lines.get(i % lines.size());
            int start = (i * 3) % (line.size() - 7);
            Station departure = line.get(start);
            Station arrival = line.get(start + 5 + i % 3);
            LocalTime first = LocalTime.of(5, 30).plusMinutes((i % 32) * 20L);
            TrainOperation operation = new TrainOperation(demoTrains.get(i % demoTrains.size()),
                    departure.getLineCode(), departure, arrival, first, first.plusMinutes(25));
            operation.assignOperationNumber(numbers.nextNumber("OPS", 5));
            demoOperations.add(operations.save(operation));
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (int i = 0; i < 45; i++) {
            OffsetDateTime occurredAt = now.minusHours(i * 4L + 1);
            Incident incident = new Incident(demoOperations.get(i % demoOperations.size()),
                    INCIDENT_TYPES[i % INCIDENT_TYPES.length], occurredAt,
                    "현황 데모 운행 장애 " + (i + 1) + "번의 영향 내용을 확인 중입니다.",
                    actor, occurredAt.plusMinutes(5));
            incident.assignIncidentNumber(numbers.nextNumber("INC", 5));
            incidents.save(incident);
            histories.save(new IncidentStatusHistory(incident, null, "장애 등록", actor, occurredAt.plusMinutes(5)));

            String action = "현장 확인 및 복구 작업을 진행했습니다.";
            for (int step = 1; step <= i % 5; step++) {
                IncidentStatus previous = incident.getStatus();
                OffsetDateTime changedAt = occurredAt.plusMinutes(5 + step * 10L);
                switch (step) {
                    case 1 -> incident.check(actor, changedAt);
                    case 2 -> incident.start(action, actor, changedAt);
                    case 3 -> incident.verifyRecovery(action, actor, changedAt);
                    case 4 -> incident.complete(action, actor, changedAt);
                    default -> throw new IllegalStateException("현황 데모 장애 상태가 올바르지 않습니다.");
                }
                histories.save(new IncidentStatusHistory(incident, previous, "현황 데모 상태 변경", actor, changedAt));
            }
        }
    }
}
