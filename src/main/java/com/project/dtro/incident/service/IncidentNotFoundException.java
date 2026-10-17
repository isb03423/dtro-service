package com.project.dtro.incident.service;

public class IncidentNotFoundException extends RuntimeException {
    // 장애를 찾지 못했을 때 사용할 오류 메시지를 설정한다.
    public IncidentNotFoundException() {
        super("장애를 찾을 수 없습니다.");
    }
}
