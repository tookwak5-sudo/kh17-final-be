# KHEdu - 학원 통합 관리 시스템

## 1. 프로젝트 소개

학원 운영에 필요한 업무를 통합적으로 관리하기 위한 웹 기반 그룹웨어 프로젝트입니다.

학생·학부모·직원 계정 관리, 강의 및 시간표, 출결, 상담 등의 기능을 제공하며 사용자 유형과 권한에 따른 접근 제어를 구현했습니다.

- **개발 기간:** 2026.08.18 ~ 2026.09.18
- **참여 인원:** 5명
- **담당 분야:** 백엔드 초기 환경 구성, 인증·인가, 강의 기능, CI/CD

## 2. 기술 스택

- **Backend:** Java 21, Spring Boot, Spring Security, MyBatis
- **Authentication:** JWT
- **Database:** Oracle 11g XE
- **API Documentation:** Swagger
- **Deployment:** Jenkins, Apache Tomcat, Ubuntu
- **Frontend:** React, Vite (별도 저장소)

## 3. 주요 구현 기능

### 인증 및 권한 관리
- Spring Security와 JWT 기반 인증·인가 구조 구축
- JWT 검증 필터를 통한 토큰 유효성 검증
- 학생·학부모·직원 유형별 인증 및 직원 역할별 권한 관리

### 강의 및 출결 관리
- 강의(Course)–시간표(Schedule)–차시(ClassSession)–출결(Attendance) 데이터 관계 설계
- 강의 등록 시 강의실 중복 예약 및 시간대 충돌 검증
- 차시별 수강생 출석부 일괄 조회 및 출결 상태 변경 API 구현

### 동시성 제어
- 동시 수강 신청에 따른 정원 초과 가능성 검토
- 비관적 락(Pessimistic Lock)을 활용한 동시 접근 제어
- 테스트 코드를 통한 동시 신청 상황 검증

### CI/CD
- Jenkins 기반 빌드 및 배포 파이프라인 구축
- 프런트엔드 정적 자원과 백엔드 애플리케이션 통합 빌드
- WAR 파일 서버 배포 및 애플리케이션 재시작 자동화

## 4. 시스템 구성

- **Frontend:** React + Vite
- **Backend:** Spring Boot + Spring Security + MyBatis
- **Database:** Oracle 11g XE
- **Deployment:** Jenkins + Apache Tomcat + Ubuntu

## 5. 관련 저장소

- **Backend:** https://github.com/tookwak5-sudo/kh17-final-be
- **Frontend:** https://github.com/tookwak5-sudo/kh17-final-fe

## 6. 담당 역할

백엔드 보일러플레이트 구성과 인증·인가 구조 구축을 담당했으며, 강의 및 출결 관련 API와 동시성 제어 로직을 구현했습니다. 또한 Jenkins 기반 CI/CD 파이프라인을 구성해 애플리케이션 빌드와 서버 배포를 자동화했습니다.
