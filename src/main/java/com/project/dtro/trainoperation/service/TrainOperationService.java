package com.project.dtro.trainoperation.service;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.station.domain.Station;
import com.project.dtro.station.dto.StationOptionResponse;
import com.project.dtro.station.repository.StationRepository;
import com.project.dtro.train.repository.TrainRepository;
import com.project.dtro.trainoperation.domain.TrainOperation;
import com.project.dtro.trainoperation.dto.*;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrainOperationService {
    private static final Set<String> SORT_FIELDS = Set.of(
            "operationNumber", "trainNumber", "lineCode", "direction",
            "departureStationName", "arrivalStationName", "firstTrainTime", "lastTrainTime",
            "createdAt", "createdByName", "updatedAt", "updatedByName"
    );

    private final TrainOperationRepository operations;
    private final TrainRepository trains;
    private final StationRepository stations;
    private final NumberGenerator numberGenerator;

    // 검색 조건과 페이지·정렬값을 검증해 운행정보 목록을 조회한다.
    @Transactional(readOnly = true)
    public OperationPageResponse search(OperationSearchRequest request) {
        if (request.getPage() < 0 || request.getSize() < 1 || request.getSize() > 100) {
            throw new IllegalArgumentException("페이지 조건이 올바르지 않습니다.");
        }
        if (request.getDirection() != null && !request.getDirection().isBlank()
                && !List.of("UP", "DOWN").contains(request.getDirection())) {
            throw new IllegalArgumentException("방향 조건이 올바르지 않습니다.");
        }

        String[] sort = request.getSort() == null ? new String[0] : request.getSort().split(",", -1);
        if (sort.length != 2 || !("id".equals(sort[0]) || SORT_FIELDS.contains(sort[0]))
                || !("asc".equalsIgnoreCase(sort[1]) || "desc".equalsIgnoreCase(sort[1]))) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }

        Specification<TrainOperation> filter = (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (request.getTrainId() != null) {
                conditions.add(cb.equal(root.get("train").get("id"), request.getTrainId()));
            }
            if (request.getLineCode() != null && !request.getLineCode().isBlank()) {
                conditions.add(cb.equal(root.get("lineCode"), request.getLineCode()));
            }
            if ("DOWN".equals(request.getDirection())) {
                conditions.add(cb.lessThan(root.get("departureStation").get("stationOrder"),
                        root.get("arrivalStation").get("stationOrder")));
            }
            if ("UP".equals(request.getDirection())) {
                conditions.add(cb.greaterThan(root.get("departureStation").get("stationOrder"),
                        root.get("arrivalStation").get("stationOrder")));
            }
            if (query.getResultType() != Long.class) {
                Expression<?> value = switch (sort[0]) {
                    case "trainNumber" -> root.join("train", JoinType.LEFT).get("trainNumber");
                    case "departureStationName" -> root.join("departureStation", JoinType.LEFT).get("stationName");
                    case "arrivalStationName" -> root.join("arrivalStation", JoinType.LEFT).get("stationName");
                    case "createdByName" -> root.join("createdBy", JoinType.LEFT).get("name");
                    case "updatedByName" -> root.join("updatedBy", JoinType.LEFT).get("name");
                    case "direction" -> cb.selectCase()
                            .when(cb.lessThan(root.get("departureStation").get("stationOrder"),
                                    root.get("arrivalStation").get("stationOrder")), "DOWN")
                            .otherwise("UP");
                    default -> root.get(sort[0]);
                };
                query.orderBy("asc".equalsIgnoreCase(sort[1]) ? cb.asc(value) : cb.desc(value),
                        cb.desc(root.get("id")));
            }
            return cb.and(conditions.toArray(Predicate[]::new));
        };
        return OperationPageResponse.from(operations.findAll(filter,
                PageRequest.of(request.getPage(), request.getSize())));
    }

    // 추가·수정한 행을 검증하고 운행정보를 일괄 저장한다.
    @Transactional
    public List<OperationResponse> save(OperationSaveRequest request) {
        List<OperationResponse> saved = new ArrayList<>();
        for (OperationRowRequest row : request.getRows()) {
            var train = trains.findById(row.getTrainId())
                    .orElseThrow(() -> new IllegalArgumentException("열차를 찾을 수 없습니다."));
            Station departure = station(row.getDepartureStationCode(), row.getLineCode());
            Station arrival = station(row.getArrivalStationCode(), row.getLineCode());
            if (departure.getStationOrder().equals(arrival.getStationOrder())) {
                throw new IllegalArgumentException("출발역과 도착역은 달라야 합니다.");
            }
            TrainOperation operation;
            if (row.getId() == null) {
                operation = new TrainOperation(train, row.getLineCode(), departure, arrival,
                        row.getFirstTrainTime(), row.getLastTrainTime());
                operation.assignOperationNumber(numberGenerator.nextNumber("OPS", 5));
                operations.save(operation);
            } else {
                operation = operations.findById(row.getId())
                        .orElseThrow(() -> new IllegalArgumentException("운행정보를 찾을 수 없습니다."));
                operation.update(train, row.getLineCode(), departure, arrival,
                        row.getFirstTrainTime(), row.getLastTrainTime());
            }
            operations.flush();
            saved.add(OperationResponse.from(operation));
        }
        return saved;
    }

    // 선택한 운행정보가 존재하는지 확인한 뒤 일괄 삭제한다.
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id < 1)) {
            throw new IllegalArgumentException("삭제할 운행정보를 선택해주세요.");
        }
        for (Long id : ids) {
            TrainOperation operation = operations.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("운행정보를 찾을 수 없습니다."));
            operations.delete(operation);
        }
        operations.flush();
    }

    // 선택 가능한 노선 코드를 조회한다.
    @Transactional(readOnly = true)
    public List<String> lines() {
        return stations.findLineCodes();
    }

    // 운행 방향에 맞게 역 목록을 정렬해 선택 항목으로 변환한다.
    @Transactional(readOnly = true)
    public List<StationOptionResponse> stations(String lineCode, String direction) {
        if (!List.of("UP", "DOWN").contains(direction)) {
            throw new IllegalArgumentException("방향 조건이 올바르지 않습니다.");
        }

        List<Station> ordered = "UP".equals(direction)
                ? stations.findByLineCodeOrderByStationOrderDesc(lineCode)
                : stations.findByLineCodeOrderByStationOrderAsc(lineCode);
        return ordered.stream().map(StationOptionResponse::from).toList();
    }

    // 역이 존재하고 선택한 노선에 속하는지 확인한다.
    private Station station(String code, String lineCode) {
        Station station = stations.findByStationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("역을 찾을 수 없습니다."));
        if (!station.getLineCode().equals(lineCode)) {
            throw new IllegalArgumentException("역과 노선이 일치하지 않습니다.");
        }
        return station;
    }
}
