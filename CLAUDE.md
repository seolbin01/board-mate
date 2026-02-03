# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Always respond in Korean.**

## Project Overview

Board-Mate is a full-stack web application for organizing board game meetups. Users can create/join gaming sessions at local cafes with real-time notifications and a trust score system for community credibility.

## Tech Stack

- **Backend**: Spring Boot 4.0.2 + Java 25 + PostgreSQL 16 + Redis 7
- **Frontend**: React 19 + TypeScript + Vite + Tailwind CSS
- **Real-time**: WebSocket (STOMP/SockJS)
- **Auth**: JWT (access + refresh tokens)

## Common Commands

### Backend (from `/backend`)

```bash
# Start infrastructure (PostgreSQL + Redis)
docker-compose up

# Build
./gradlew build

# Run (starts on http://localhost:8080)
./gradlew bootRun

# Run all tests
./gradlew test

# Run single test class
./gradlew test --tests "ClassName"

# Run single test method
./gradlew test --tests "ClassName.methodName"
```

### Frontend (from `/frontend`)

```bash
# Install dependencies
npm install

# Dev server (http://localhost:5173)
npm run dev

# Build (TypeScript compile + Vite build)
npm run build

# Lint
npm run lint
```

## Architecture

### Backend Structure

```
backend/src/main/java/com/benny/board_mate/
├── auth/           # JWT authentication (signup, login, token refresh)
├── user/           # User entity & repository
├── room/           # Room CRUD, host management
├── participant/    # Join/leave rooms, attendance tracking
├── game/           # Board game catalog (read-only)
├── trust/          # Trust score calculation (+10 attended, -30 no-show)
├── notification/   # WebSocket STOMP notifications
└── common/         # Config, exceptions, BaseEntity (soft delete)
```

**Layered Pattern**: Controller → Service → Repository → Entity

**Key Patterns**:
- DTOs for API contracts (Request/Response classes)
- Custom `BusinessException` with `ErrorCode` enum for error handling
- `@Version` for optimistic locking on Room entity
- Soft delete via `deletedAt` timestamp in BaseEntity

### Frontend Structure

```
frontend/src/
├── pages/          # Route page components
├── components/     # Reusable UI (Layout, etc.)
├── api/            # Axios client with JWT interceptor
├── stores/         # Zustand state (authStore)
├── types/          # TypeScript interfaces
└── hooks/          # Custom React hooks
```

**State**: Zustand for auth state, tokens in localStorage

**Real-time**: STOMP subscriptions to `/topic/rooms/{roomId}` for participant updates

### Domain Relationships

```
User (1) ←→ (N) Room (as host)
User (1) ←→ (1) TrustScore
Room (N) ←→ (1) BoardGame
Room (1) ←→ (N) Participant ←→ (N) User
```

Participant has `AttendanceStatus`: PENDING, ATTENDED, NO_SHOW, CANCELLED

## API Endpoints

- Auth: `POST /api/auth/signup`, `POST /api/auth/login`
- Rooms: `GET|POST /api/rooms`, `GET|DELETE /api/rooms/{id}`
- Participants: `POST|DELETE|GET /api/rooms/{id}/participants`
- Attendance: `POST /api/rooms/{id}/participants/attendance`
- Games: `GET /api/games` (public)
- Trust: `GET /api/trust-scores/{userId}`

WebSocket: Connect to `/ws`, subscribe to `/topic/rooms/{roomId}`

## Configuration

- Backend config: `backend/src/main/resources/application.yml`
- Database: PostgreSQL on port 5400, database name `boardmate`
- Redis: Port 6300
- CORS: Allows `http://localhost:5173`
- Initial game data loaded from `data.sql`

## Testing Notes

- Backend uses JUnit5 with TestContainers for integration tests
- Concurrency tests exist for pessimistic/optimistic locking scenarios (100 concurrent requests)
- WebSocket notification tests available
