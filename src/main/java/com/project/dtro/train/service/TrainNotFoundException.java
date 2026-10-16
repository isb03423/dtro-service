package com.project.dtro.train.service;

public class TrainNotFoundException extends RuntimeException {
    // 열차를 찾지 못했을 때 사용할 오류 메시지를 설정한다.
    public TrainNotFoundException() {
        super("열차 정보를 찾을 수 없습니다.");
    }
}
