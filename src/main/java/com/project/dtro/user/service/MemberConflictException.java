package com.project.dtro.user.service;

public class MemberConflictException extends RuntimeException {
    public MemberConflictException() {
        super("이미 사용 중인 아이디입니다.");
    }
}
