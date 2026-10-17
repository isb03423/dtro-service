-- 기존 개발 DB의 운행 장애 상태 제약조건을 5단계로 갱신한다.
BEGIN;

ALTER TABLE incident DROP CONSTRAINT IF EXISTS incident_status_check;
ALTER TABLE incident ADD CONSTRAINT incident_status_check
    CHECK (status IN ('RECEIVED', 'CHECKING', 'IN_PROGRESS', 'VERIFYING', 'COMPLETED'));

ALTER TABLE incident_status_history DROP CONSTRAINT IF EXISTS incident_status_history_new_status_check;
ALTER TABLE incident_status_history ADD CONSTRAINT incident_status_history_new_status_check
    CHECK (new_status IN ('RECEIVED', 'CHECKING', 'IN_PROGRESS', 'VERIFYING', 'COMPLETED'));

ALTER TABLE incident_status_history DROP CONSTRAINT IF EXISTS incident_status_history_previous_status_check;
ALTER TABLE incident_status_history ADD CONSTRAINT incident_status_history_previous_status_check
    CHECK (previous_status IN ('RECEIVED', 'CHECKING', 'IN_PROGRESS', 'VERIFYING', 'COMPLETED'));

COMMIT;
