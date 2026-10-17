package com.project.dtro.incident.service;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.incident.domain.Incident;
import com.project.dtro.incident.domain.IncidentStatus;
import com.project.dtro.incident.domain.IncidentStatusHistory;
import com.project.dtro.incident.dto.IncidentActionRequest;
import com.project.dtro.incident.dto.IncidentDeleteRequest;
import com.project.dtro.incident.dto.IncidentHistoryResponse;
import com.project.dtro.incident.dto.IncidentPageResponse;
import com.project.dtro.incident.dto.IncidentResponse;
import com.project.dtro.incident.dto.IncidentSaveRequest;
import com.project.dtro.incident.dto.IncidentSearchRequest;
import com.project.dtro.incident.repository.IncidentRepository;
import com.project.dtro.incident.repository.IncidentStatusHistoryRepository;
import com.project.dtro.trainoperation.domain.TrainOperation;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import com.project.dtro.user.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "incidentNumber", "operationNumber", "incidentType", "occurredAt",
            "status", "createdAt", "createdByName", "updatedAt", "updatedByName");

    private final IncidentRepository incidents;
    private final IncidentStatusHistoryRepository histories;
    private final TrainOperationRepository operations;
    private final AuditorAware<Member> auditorAware;
    private final Clock clock;
    private final NumberGenerator numberGenerator;

    // 검색 조건과 페이지·정렬값을 검증해 운행 장애 목록을 조회한다.
    @Transactional(readOnly = true)
    public IncidentPageResponse search(IncidentSearchRequest request) {
        if (request.getPage() < 0 || request.getSize() < 1 || request.getSize() > 100) {
            throw new IllegalArgumentException("페이지 조건이 올바르지 않습니다.");
        }

        Sort order = Sort.by(Sort.Direction.DESC, "id");
        if (request.getSort() != null && !request.getSort().isBlank()) {
            String[] sort = request.getSort().split(",", -1);
            if (sort.length != 2 || !SORT_FIELDS.contains(sort[0])
                    || !("asc".equalsIgnoreCase(sort[1]) || "desc".equalsIgnoreCase(sort[1]))) {
                throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
            }

            String property = switch (sort[0]) {
                case "operationNumber" -> "operation.operationNumber";
                case "createdByName" -> "createdBy.name";
                case "updatedByName" -> "updatedBy.name";
                default -> sort[0];
            };
            order = Sort.by(Sort.Direction.fromString(sort[1]), property);
            if (!"id".equals(sort[0])) {
                order = order.and(Sort.by(Sort.Direction.DESC, "id"));
            }
        }

        Specification<Incident> filter = (root, query, cb) -> {
            var conditions = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (request.getOperationId() != null) {
                conditions.add(cb.equal(root.get("operation").get("id"), request.getOperationId()));
            }
            if (request.getIncidentType() != null && !request.getIncidentType().isBlank()) {
                conditions.add(cb.equal(root.get("incidentType"), request.getIncidentType()));
            }
            if (request.getStatus() != null) {
                conditions.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(conditions.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return IncidentPageResponse.from(incidents.findAll(filter,
                PageRequest.of(request.getPage(), request.getSize(), order)));
    }

    // ID로 운행 장애를 찾고 상세 응답으로 변환한다.
    @Transactional(readOnly = true)
    public IncidentResponse get(Long id) {
        return IncidentResponse.from(find(id));
    }

    // 해당 장애의 상태 변경 이력을 최신순으로 조회한다.
    @Transactional(readOnly = true)
    public List<IncidentHistoryResponse> history(Long id) {
        find(id);
        return histories.findByIncidentIdOrderByChangedAtDescIdDesc(id).stream()
                .map(IncidentHistoryResponse::from).toList();
    }

    // 운행 장애를 접수하고 최초 상태 이력을 함께 저장한다.
    @Transactional
    public IncidentResponse create(IncidentSaveRequest request) {
        Member actor = actor();
        OffsetDateTime now = OffsetDateTime.now(clock);
        Incident incident = new Incident(operation(request.getOperationId()), request.getIncidentType().trim(),
                request.getOccurredAt(), request.getImpactDetail().trim(), actor, now);
        incident.assignIncidentNumber(numberGenerator.nextNumber("INC", 5));
        incidents.saveAndFlush(incident);
        histories.save(new IncidentStatusHistory(incident, null, "장애 등록", actor, now));
        return IncidentResponse.from(incident);
    }

    // 접수 내용을 수정하고 버전으로 동시 변경을 확인한다.
    @Transactional
    public IncidentResponse update(Long id, IncidentSaveRequest request) {
        Incident incident = find(id);
        checkVersion(incident, request.getVersion());
        incident.updateReception(operation(request.getOperationId()), request.getIncidentType().trim(),
                request.getOccurredAt(), request.getImpactDetail().trim(), actor(), OffsetDateTime.now(clock));
        incidents.flush();
        return IncidentResponse.from(incident);
    }

    // 조치 내용과 상태 변경 이력을 하나의 트랜잭션으로 저장한다.
    @Transactional
    public IncidentResponse action(Long id, String command, IncidentActionRequest request) {
        Incident incident = find(id);
        checkVersion(incident, request.getVersion());
        Member actor = actor();
        OffsetDateTime now = OffsetDateTime.now(clock);
        IncidentStatus previous = incident.getStatus();
        String content = blankToNull(request.getActionDetail());
        String defaultReason;
        switch (command) {
            case "check" -> {
                incident.check(actor, now);
                defaultReason = "확인 시작";
            }
            case "start" -> {
                incident.start(content, actor, now);
                defaultReason = "조치 시작";
            }
            case "save" -> {
                incident.saveAction(content, actor, now);
                defaultReason = "조치 내용 저장";
            }
            case "verify" -> {
                incident.verifyRecovery(content, actor, now);
                defaultReason = "복구 확인 시작";
            }
            case "resume" -> {
                incident.resume(content, actor, now);
                defaultReason = "추가 조치";
            }
            case "complete" -> {
                incident.complete(content, actor, now);
                defaultReason = "조치 완료";
            }
            default -> throw new IllegalArgumentException("조치 명령이 올바르지 않습니다.");
        }
        if (previous != incident.getStatus()) {
            String reason = request.getReason() == null || request.getReason().isBlank()
                    ? defaultReason : request.getReason().trim();
            histories.save(new IncidentStatusHistory(incident, previous, reason, actor, now));
        }
        incidents.flush();
        return IncidentResponse.from(incident);
    }

    // 완료 건과 버전을 모두 확인한 뒤 장애 및 이력을 일괄 삭제한다.
    @Transactional
    public void delete(List<IncidentDeleteRequest> rows) {
        if (rows == null || rows.isEmpty() || rows.stream().anyMatch(row -> row == null
                || row.getId() == null || row.getId() < 1 || row.getVersion() == null || row.getVersion() < 0)) {
            throw new IllegalArgumentException("삭제할 장애를 선택해주세요.");
        }
        if (rows.stream().map(IncidentDeleteRequest::getId).distinct().count() != rows.size()) {
            throw new IllegalArgumentException("삭제할 장애가 중복되었습니다.");
        }

        // 전체 선택 건을 먼저 확인하여 완료 건이 포함되면 삭제하지 않는다.
        List<Incident> targets = rows.stream().map(row -> {
            Incident incident = find(row.getId());
            checkVersion(incident, row.getVersion());
            if (incident.getStatus() == IncidentStatus.COMPLETED) {
                throw new IllegalStateException("조치 완료된 장애는 삭제할 수 없습니다.");
            }
            return incident;
        }).toList();
        for (Incident incident : targets) {
            histories.deleteByIncidentId(incident.getId());
            incidents.delete(incident);
        }
        incidents.flush();
    }

    // ID에 해당하는 장애를 찾거나 없음 오류를 던진다.
    private Incident find(Long id) {
        return incidents.findById(id).orElseThrow(IncidentNotFoundException::new);
    }

    // 접수에 연결할 운행정보가 존재하는지 확인한다.
    private TrainOperation operation(Long id) {
        return operations.findById(id).orElseThrow(() -> new IllegalArgumentException("운행정보를 찾을 수 없습니다."));
    }

    // 현재 작업자의 회원 정보를 가져온다.
    private Member actor() {
        return auditorAware.getCurrentAuditor().orElseThrow(() -> new IllegalStateException("로그인 정보를 확인해주세요."));
    }

    // 화면에서 조회한 버전과 현재 버전이 일치하는지 검사한다.
    private void checkVersion(Incident incident, Long version) {
        if (version == null || !Objects.equals(incident.getVersion(), version)) {
            throw new IllegalStateException("다른 사용자가 변경한 장애입니다. 새로 조회한 뒤 다시 처리해주세요.");
        }
    }

    // 공백 문자열을 null로 바꾸고 나머지 문자열은 다듬는다.
    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
