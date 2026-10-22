# DTRO Service

도시철도 운영 정보와 고객 서비스 데이터를 제공하는 DTRO의 Spring Boot REST API입니다. 사용자 화면은 별도 저장소인 [dtro-web](https://github.com/isb03423/dtro-web)에서 제공합니다.

## 목차

1. [프로젝트 소개](#프로젝트-소개)
2. [주요 기능](#주요-기능)
3. [프로젝트 구조](#프로젝트-구조)
4. [실행 전 준비](#실행-전-준비)
5. [1단계: 소스코드 다운로드](#1단계-소스코드-다운로드)
6. [2단계: PostgreSQL 데이터베이스 준비](#2단계-postgresql-데이터베이스-준비)
7. [3단계: Spring Boot 실행](#3단계-spring-boot-실행)
8. [Gradle로 실행 파일 만들기](#gradle로-실행-파일-만들기)
9. [자주 발생하는 오류](#자주-발생하는-오류)
10. [보안 및 배포 전 확인사항](#보안-및-배포-전-확인사항)

## 프로젝트 소개

- **기술 스택:** Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, PostgreSQL, Gradle
- **API 주소:** 로컬 실행 시 `http://localhost:8080/api`
- **프론트엔드:** [dtro-web](https://github.com/isb03423/dtro-web) (Vue 3, Vite)

## 주요 기능

- 열차 정보, 운행 정보, 열차 현황 및 운행 장애 API
- 역 정보 초기 데이터와 열차·장애 번호 관리
- 공지사항, 자료실, Q&A, 민원 및 고객 서비스 현황 API
- 회원가입, 로그인, 세션 인증, 역할별 접근 제어 및 관리자 회원 관리
- 자료실 첨부 파일 저장

## 프로젝트 구조

```text
src/
├─ main/
│  ├─ java/com/project/dtro/
│  │  ├─ auth/, security/, user/       # 인증, 보안, 회원
│  │  ├─ train/, trainoperation/       # 열차와 운행 정보
│  │  ├─ incident/, station/           # 장애와 역 정보
│  │  ├─ complaint/, qna/              # 고객 문의
│  │  ├─ notice/, resource/            # 공지사항과 자료실
│  │  ├─ customerservice/, admin/      # 현황과 관리자 기능
│  │  └─ common/                       # 공통 응답, 설정, 번호 생성
│  └─ resources/
│     ├─ application.yaml              # 기본 실행 설정
│     ├─ application-dev.yaml          # 개발 프로필의 데모 데이터 설정
│     └─ stations.csv                  # 역 초기 데이터
└─ test/                               # 테스트 코드
gradlew, gradlew.bat                    # Gradle Wrapper
```

## 실행 전 준비

1. Git, Java 21 JDK, PostgreSQL을 설치합니다. Gradle은 저장소의 Wrapper로 실행하므로 별도 설치가 필요하지 않습니다.
2. `git --version`, `java -version`, `psql --version`으로 설치를 확인합니다. `java -version`에는 Java 21이 표시되어야 합니다.
3. PostgreSQL 서버가 로컬 `5432` 포트에서 실행 중인지 확인합니다.

## 1단계: 소스코드 다운로드

```bash
git clone https://github.com/isb03423/dtro-service.git
cd dtro-service
```

이미 저장소가 있다면 해당 디렉터리에서 다음 명령을 실행합니다.

## 2단계: PostgreSQL 데이터베이스 준비

PostgreSQL에서 `dtro` 데이터베이스를 만듭니다. 이미 존재한다면 생략합니다.

```bash
psql -h localhost -U postgres -c 'CREATE DATABASE dtro;'
```

기본 연결 정보는 [`src/main/resources/application.yaml`](src/main/resources/application.yaml)에 있습니다. 현재 설정은 `localhost:5432/dtro`와 사용자 `postgres`를 사용합니다. 로컬 PostgreSQL 계정의 비밀번호가 다르면 실행 전에 `SPRING_DATASOURCE_PASSWORD` 환경 변수로 지정합니다. 실제 비밀번호를 README나 Git에 기록하지 마세요.

## 3단계: Spring Boot 실행

Git Bash 또는 macOS/Linux 터미널:

```bash
export SPRING_DATASOURCE_PASSWORD='본인 PostgreSQL 비밀번호'
./gradlew bootRun
```

Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = '본인 PostgreSQL 비밀번호'
.\gradlew.bat bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다. 웹 개발 서버는 `/api` 요청을 이 포트로 전달합니다. 최초 실행 시 JPA의 `ddl-auto: update` 설정에 따라 필요한 테이블이 생성·갱신되고, `stations.csv`의 누락된 역 정보가 등록됩니다. 개발용 계정 생성 코드도 실행되므로 실제 운영 데이터베이스에서 이 설정을 그대로 사용하지 마세요.

열차 현황의 데모 데이터를 생성하려면 개발 프로필을 명시해 실행합니다. 이 프로필은 [`application-dev.yaml`](src/main/resources/application-dev.yaml)의 `dtro.demo-data.enabled`를 켭니다.

```bash
SPRING_DATASOURCE_PASSWORD='본인 PostgreSQL 비밀번호' SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

## Gradle로 실행 파일 만들기

```bash
./gradlew bootJar
java -jar build/libs/dtro-0.0.1-SNAPSHOT.jar
```

Windows PowerShell에서는 `./gradlew` 대신 `.\gradlew.bat`를 사용합니다. `bootJar` 결과는 `build/libs/`에 저장됩니다. 테스트까지 포함한 전체 빌드가 필요하면 `./gradlew build`를 실행합니다. JAR 실행에도 PostgreSQL 연결 설정이 필요합니다.

## 자주 발생하는 오류

| 증상 | 확인할 내용 |
| --- | --- |
| 데이터베이스 연결 실패 | PostgreSQL 실행 여부, `dtro` 데이터베이스, `5432` 포트, 계정·비밀번호를 확인합니다. |
| `Port 8080 was already in use` | 해당 포트를 사용 중인 프로그램을 종료하거나 서비스 포트를 바꾸고 웹의 Vite 프록시도 함께 수정합니다. |
| Java 또는 Gradle 실행 오류 | Java 21 JDK와 `JAVA_HOME`을 확인하고 저장소의 Gradle Wrapper를 사용합니다. |
| 웹에서 401/403 응답 | 로그인 상태와 계정 역할을 확인합니다. 변경 요청은 CSRF 토큰도 필요합니다. |
| 첨부 파일을 찾을 수 없음 | `file.upload-dir`의 기본값인 실행 디렉터리 아래 `uploads/` 경로와 파일 접근 권한을 확인합니다. |

## 보안 및 배포 전 확인사항

- [`application.yaml`](src/main/resources/application.yaml)에 DB 비밀번호가 직접 지정되어 있습니다. 배포 전 저장소에서 비밀번호를 제거하고 외부 설정으로 공급하며, 이미 사용한 비밀번호는 교체해야 합니다.
- 개발용 계정을 생성하는 [`DevAccountsSeed`](src/main/java/com/project/dtro/auth/config/DevAccountsSeed.java)는 현재 프로필 조건 없이 실행됩니다. 운영 환경에서는 비활성화하거나 제거하고 기본 비밀번호를 사용하지 않아야 합니다.
- 개발 프로필의 데모 데이터 생성 설정을 운영 환경에서 켜지 않습니다.
- `ddl-auto: update`와 `show-sql: true`는 운영 데이터베이스와 로그 정책에 맞춰 검토해야 합니다.
- 첨부 파일 저장 경로와 CORS 허용 출처를 배포 환경에 맞게 설정합니다.

현재 저장소에는 Dockerfile이나 Compose 설정이 없어 Docker 실행 절차는 포함하지 않았습니다.
