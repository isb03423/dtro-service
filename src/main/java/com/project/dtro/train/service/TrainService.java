package com.project.dtro.train.service;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import com.project.dtro.train.dto.TrainPageResponse;
import com.project.dtro.train.dto.TrainRequest;
import com.project.dtro.train.dto.TrainResponse;
import com.project.dtro.train.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrainService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "trainNumber", "formationName", "status", "lastInspectedOn", "updatedAt");

    private final TrainRepository repository;
    private final NumberGenerator numberGenerator;

    // 검색 조건과 페이지·정렬값을 검증해 열차 목록을 조회한다.
    @Transactional(readOnly = true)
    public TrainPageResponse search(TrainStatus status, String keyword, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 조건이 올바르지 않습니다.");
        }

        // 허용된 필드와 asc/desc만 사용해 임의의 정렬값을 막는다.
        String[] parts = sort.split(",", -1);
        if (parts.length != 2 || !SORT_FIELDS.contains(parts[0])
                || !(parts[1].equalsIgnoreCase("asc") || parts[1].equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }

        Sort.Direction direction = Sort.Direction.fromString(parts[1]);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, parts[0]));
        String requestedKeyword = blankToNull(keyword);
        // 값이 있는 조건만 모아 AND로 연결해 동적 검색 쿼리를 만든다.
        Specification<Train> filter = (root, query, cb) -> {
            var conditions = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null) {
                conditions.add(cb.equal(root.get("status"), status));
            }

            if (requestedKeyword != null) {
                String pattern = "%" + requestedKeyword.toLowerCase(Locale.ROOT) + "%";
                // 검색어는 열차번호 또는 편성명 중 하나에 포함되면 일치한다.
                conditions.add(cb.or(cb.like(cb.lower(root.get("trainNumber")), pattern),
                        cb.like(cb.lower(root.get("formationName")), pattern)));
            }

            return cb.and(conditions.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return TrainPageResponse.from(repository.findAll(filter, pageable));
    }

    // ID로 열차를 찾고 응답 형식으로 변환한다.
    @Transactional(readOnly = true)
    public TrainResponse get(Long id) {
        return TrainResponse.from(find(id));
    }

    // 입력값으로 열차를 저장하고 생성된 열차번호를 반환한다.
    @Transactional
    public TrainResponse create(TrainRequest request) {
        Train train = new Train(
                request.getFormationName().trim(),
                request.getCarType().trim(),
                request.getCarCount(),
                request.getStatus(),
                request.getIntroducedOn(),
                request.getLastInspectedOn()
        );

        // 열차 ID와 관계없이 해당 호선의 다음 번호를 발급한다.
        train.assignTrainNumber(numberGenerator.nextNumber("TRN", 4));
        repository.save(train);
        repository.flush();
        return TrainResponse.from(train);
    }

    // 기존 열차의 필드를 변경하고 저장한다.
    @Transactional
    public TrainResponse update(Long id, TrainRequest request) {
        Train train = find(id);

        train.update(
                request.getFormationName().trim(),
                request.getCarType().trim(),
                request.getCarCount(),
                request.getStatus(),
                request.getIntroducedOn(),
                request.getLastInspectedOn()
        );
        repository.flush();
        return TrainResponse.from(train);
    }

    // 열차가 존재하는지 확인한 뒤 삭제한다.
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
        repository.flush();
    }

    // ID에 해당하는 열차를 찾거나 없음 오류를 던진다.
    private Train find(Long id) {
        return repository.findById(id).orElseThrow(TrainNotFoundException::new);
    }

    // 공백 문자열을 null로 바꾸고 나머지 문자열은 다듬는다.
    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
