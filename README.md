# 🎲 Board-Mate

> 보드게임 모임을 더 쉽게, 더 즐겁게

Board-Mate는 보드게임을 좋아하는 사람들이 쉽게 모임을 만들고 참여할 수 있는 매칭 플랫폼입니다.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)

---

## ✨ 주요 기능

### 🏠 방 생성 및 참가
- 원하는 게임, 지역, 날짜로 모임 생성
- 실시간 참가자 현황 확인
- 무한 스크롤로 편리한 방 목록 탐색

### 🔍 검색 및 필터
- 지역, 게임, 날짜별 필터링
- 페이지네이션 적용으로 빠른 로딩

### 📊 신뢰도 시스템
- 출석/노쇼 기반 신뢰도 점수
- S~F 등급으로 사용자 신뢰도 표시
- 건강한 커뮤니티 문화 조성

### 💬 실시간 채팅
- 방 내 참가자들과 실시간 채팅
- WebSocket(STOMP) 기반 즉시 메시지 전송
- 채팅 내역 저장 및 조회

### ⭐ 리뷰 및 평점
- 함께 플레이한 유저 평가
- 별점(1~5) + 코멘트 작성
- 유저별 평균 평점 및 리뷰 요약

### 🔔 실시간 알림
- WebSocket 기반 실시간 알림
- 참가자 입/퇴장 즉시 반영
- 게임 시작 전 리마인더 알림

---

## 🛠 기술 스택

### Backend

| Category | Technology |
|----------|------------|
| Framework | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-6DB33F?logo=springboot&logoColor=white) |
| Language | ![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white) |
| Database | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white) |
| Cache | ![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white) |
| Auth | ![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white) |
| Real-time | ![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101) |
| Build | ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) |
| Docs | ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black) |

### Frontend

| Category | Technology |
|----------|------------|
| Framework | ![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white) |
| Language | ![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white) |
| Build | ![Vite](https://img.shields.io/badge/Vite-7.2-646CFF?logo=vite&logoColor=white) |
| Styling | ![Tailwind CSS](https://img.shields.io/badge/Tailwind-4.1-06B6D4?logo=tailwindcss&logoColor=white) |
| State | ![Zustand](https://img.shields.io/badge/Zustand-5.0-433E38) |
| Icons | ![Lucide](https://img.shields.io/badge/Lucide-Icons-F56565) |
| HTTP | ![Axios](https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=white) |

### Infrastructure

| Category | Technology |
|----------|------------|
| Container | ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) |
| Test | ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white) ![TestContainers](https://img.shields.io/badge/TestContainers-2496ED) |

---

## 📸 스크린샷

### 방 목록 (무한 스크롤)
![방 목록](docs/images/room-list.png)

### 방 상세 (실시간 참가자)
![방 상세](docs/images/room-detail.png)

---

## 🏗 아키텍처

```mermaid
flowchart LR
    subgraph Client["🌐 Browser"]
        FE["⚛️ React 19\nTypeScript + Vite"]
    end

    subgraph Vercel["☁️ Vercel"]
        STATIC["📦 Static Hosting"]
    end

    subgraph Railway["🚂 Railway"]
        BE["🍃 Spring Boot 4.0"]

        AUTH["🔐 Auth"]
        ROOM["🏠 Room"]
        CHAT["💬 Chat"]
        NOTIFY["🔔 Notification"]
    end

    subgraph DB["💾 Database"]
        PG[("🐘 PostgreSQL")]
        REDIS[("⚡ Redis")]
    end

    Client -->|HTTPS| Vercel
    Vercel -->|REST API| BE
    Client <-->|WebSocket| BE

    BE --> AUTH & ROOM & CHAT & NOTIFY

    BE -->|JPA| PG
    BE -->|Cache| REDIS

    style Client fill:#dbeafe,stroke:#3b82f6,color:#1e3a5f
    style Vercel fill:#f3e8ff,stroke:#9333ea,color:#581c87
    style Railway fill:#dcfce7,stroke:#22c55e,color:#166534
    style DB fill:#fef3c7,stroke:#f59e0b,color:#92400e
    style PG fill:#dbeafe,stroke:#3b82f6,color:#1e3a5f
    style REDIS fill:#fee2e2,stroke:#ef4444,color:#991b1b
```

### 도메인 모델

```mermaid
erDiagram
    USER ||--o{ PARTICIPANT : "1:N"
    USER ||--|| TRUST_SCORE : "1:1"
    USER ||--o{ REVIEW : "writes"
    USER ||--o{ CHAT_MESSAGE : "sends"

    ROOM ||--o{ PARTICIPANT : "N:1"
    ROOM }o--|| BOARD_GAME : "N:1"
    ROOM ||--o{ REVIEW : "has"
    ROOM ||--o{ CHAT_MESSAGE : "contains"

    USER {
        Long id PK
        String email
        String nickname
        String password
    }

    ROOM {
        Long id PK
        String region
        LocalDateTime gameDate
        Int maxParticipants
        RoomStatus status
    }

    PARTICIPANT {
        Long id PK
        AttendanceStatus status
        LocalDateTime joinedAt
    }

    TRUST_SCORE {
        Long id PK
        Int score
        String grade
        Int attendedCount
        Int noShowCount
    }

    BOARD_GAME {
        Long id PK
        String title
        Int minPlayers
        Int maxPlayers
    }

    REVIEW {
        Long id PK
        Int rating
        String comment
    }

    CHAT_MESSAGE {
        Long id PK
        String content
        LocalDateTime createdAt
    }
```

---

## 🚀 실행 방법

### 요구사항
- Java 25+
- Node.js 20+
- Docker & Docker Compose

### 1. 저장소 클론
```bash
git clone https://github.com/your-username/board-mate.git
cd board-mate
```

### 2. 인프라 실행 (PostgreSQL + Redis)
```bash
cd backend
docker-compose up -d
```

### 3. 백엔드 실행
```bash
cd backend
./gradlew bootRun
# http://localhost:8080
```

### 4. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

### 5. 테스트 데이터 생성 (선택)
```bash
./scripts/generate-test-data.sh
# 테스트 계정: testuser1@test.com ~ testuser5@test.com
# 비밀번호: test1234
```

---

## 📁 프로젝트 구조

```
board-mate/
├── backend/
│   ├── src/main/java/com/benny/board_mate/
│   │   ├── auth/           # 인증 (JWT)
│   │   ├── user/           # 사용자
│   │   ├── room/           # 방 관리
│   │   ├── participant/    # 참가자/출석
│   │   ├── game/           # 보드게임 카탈로그
│   │   ├── trust/          # 신뢰도 시스템
│   │   ├── chat/           # 실시간 채팅
│   │   ├── review/         # 리뷰/평점 시스템
│   │   ├── notification/   # WebSocket 알림
│   │   └── common/         # 공통 (config, exception)
│   ├── docker-compose.yml
│   └── build.gradle
│
├── frontend/
│   ├── src/
│   │   ├── pages/          # 페이지 컴포넌트
│   │   ├── components/     # 공용 컴포넌트
│   │   ├── api/            # API 클라이언트
│   │   ├── stores/         # Zustand 상태
│   │   ├── types/          # TypeScript 타입
│   │   └── hooks/          # 커스텀 훅
│   ├── package.json
│   └── vite.config.ts
│
└── scripts/
    └── generate-test-data.sh
```

---

## 📝 API 문서

서버 실행 후 Swagger UI에서 확인:
```
http://localhost:8080/swagger-ui.html
```

### 주요 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| GET | `/api/rooms` | 방 목록 (페이징 + 필터) |
| POST | `/api/rooms` | 방 생성 |
| GET | `/api/rooms/{id}` | 방 상세 |
| POST | `/api/rooms/{id}/participants` | 참가 |
| DELETE | `/api/rooms/{id}/participants` | 퇴장 |
| GET | `/api/rooms/{id}/chats` | 채팅 내역 조회 |
| POST | `/api/rooms/{id}/chats` | 메시지 전송 |
| POST | `/api/reviews` | 리뷰 작성 |
| GET | `/api/users/{id}/reviews` | 유저 리뷰 조회 |
| GET | `/api/games` | 게임 목록 |
| WS | `/ws` | WebSocket 연결 |

---

## 🧪 테스트

```bash
# 백엔드 테스트
cd backend
./gradlew test

# 프론트엔드 타입 체크
cd frontend
npm run lint
```

---

## 📄 라이선스

MIT License
