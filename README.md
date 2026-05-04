# Incident Management System (IMS)

A production-grade, mission-critical incident management platform built with modern distributed systems architecture.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![React](https://img.shields.io/badge/React-18-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)
- [Scaling Strategy](#scaling-strategy)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INCIDENT MANAGEMENT SYSTEM                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌────────────┐ │
│  │  Client  │────▶│  Spring Boot │────▶│   Kafka     │────▶│  Workers   │ │
│  │ (React)  │     │    API       │     │   Queue     │     │  (Async)   │ │
│  └──────────┘     └──────────────┘     └─────────────┘     └────────────┘ │
│                           │                   │                   │          │
│                           │                   │                   │          │
│                    ┌──────▼──────┐     ┌──────▼──────┐     ┌──────▼──────┐ │
│                    │    Redis     │     │  PostgreSQL │     │   MongoDB   │ │
│                    │  (Rate Limit │     │  (Incidents │     │   (Signals  │ │
│                    │  & Debounce) │     │   & RCA)    │     │   & Audit)  │ │
│                    └──────────────┘     └─────────────┘     └─────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Core Components

| Component | Purpose | Technology |
|-----------|---------|-------------|
| **Signal Ingestion API** | High-throughput signal ingestion (10K/sec) | Spring Boot + Kafka |
| **State Machine** | Incident workflow management | State Pattern |
| **Alert Strategy** | Severity determination | Strategy Pattern |
| **Debounce Manager** | Prevent duplicate incidents | Redis TTL |
| **Rate Limiter** | Backpressure handling | Redis + Lua |
| **Frontend** | Dashboard & incident management | React + TailwindCSS |

---

## 🛠️ Tech Stack

### Backend
- **Java 21** - Latest LTS with virtual threads
- **Spring Boot 3.5** - Modern reactive framework
- **PostgreSQL** - Primary data store (incidents, RCA)
- **MongoDB** - Document store (raw signals, audit logs)
- **Redis** - Caching, rate limiting, debounce state
- **Kafka** - Event streaming for async processing

### Frontend
- **React 18** - UI framework
- **Vite** - Build tool
- **TailwindCSS** - Styling
- **React Router** - Navigation
- **Axios** - HTTP client
- **date-fns** - Date formatting

### Infrastructure
- **Docker Compose** - Container orchestration
- **Maven** - Build tool

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Node.js 18+ (for frontend development)

### Quick Start

```bash
# Clone and navigate to project
cd incident-management-system

# Start all services with Docker Compose
docker-compose up -d

# Verify services are running
docker-compose ps
```

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:8081 | - |
| **Frontend** | http://localhost:3000 | - |
| **PostgreSQL** | localhost:5432 | ims_user / ims_password |
| **MongoDB** | localhost:27017 | ims_mongo / ims_mongo_password |
| **Redis** | localhost:6379 | - |
| **Kafka** | localhost:9092 | - |

### Development Mode

```bash
# Backend
./mvnw spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev
```

---

## 📚 API Documentation

### Signal Endpoints

#### POST /api/signals
Ingest a new signal (async, non-blocking).

```bash
curl -X POST http://localhost:8081/api/signals \
  -H "Content-Type: application/json" \
  -d '{
    "componentId": "CACHE_CLUSTER_01",
    "componentType": "CACHE",
    "severity": "P2",
    "message": "Redis latency spike detected",
    "timestamp": "2026-04-29T10:00:00Z"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Signal accepted for processing",
  "data": "SIG-abc12345",
  "timestamp": "2026-04-29T10:00:00Z",
  "errorCode": null
}
```

### Incident Endpoints

#### GET /api/incidents/active
Get all active incidents.

```bash
curl http://localhost:8081/api/incidents/active
```

#### GET /api/incidents/{id}
Get incident by ID.

```bash
curl http://localhost:8081/api/incidents/1
```

#### PATCH /api/incidents/{id}/status
Update incident status.

```bash
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "INVESTIGATING",
    "reason": "Team assigned",
    "updatedBy": "john@example.com"
  }'
```

#### POST /api/incidents/{id}/rca
Submit RCA for incident.

```bash
curl -X POST http://localhost:8081/api/incidents/1/rca \
  -H "Content-Type: application/json" \
  -d '{
    "rootCauseCategory": "HARDWARE",
    "incidentStartTime": "2026-04-29T10:00:00Z",
    "incidentEndTime": "2026-04-29T11:30:00Z",
    "fixApplied": "Replaced failed node",
    "preventionSteps": "Added monitoring alerts"
  }'
```

### Health Endpoints

#### GET /api/health
System health check.

```bash
curl http://localhost:8081/api/health
```

---

## 🎯 Design Decisions

### 1. Async Signal Processing

**Decision:** Use Kafka as buffering layer between API and persistence.

**Rationale:**
- Prevents API crashes when persistence layers slow down
- Provides natural backpressure - producers slow down when consumers lag
- Enables horizontal scaling of workers
- Decouples ingestion from processing

**Implementation:**
```
Client → API → Kafka → Worker → PostgreSQL/MongoDB
```

### 2. Redis Debouncing

**Decision:** Use Redis TTL keys for debounce state.

**Rationale:**
- Distributed - works across multiple API instances
- Automatic expiration - no cleanup needed
- Atomic operations - no race conditions

**Implementation:**
```
debounce:CACHE_CLUSTER_01 → INC-12345678 (TTL: 10s)
```

If 100 signals arrive within 10 seconds for same component, only ONE incident is created.

### 3. State Pattern

**Decision:** Implement explicit state handlers for incident workflow.

**Rationale:**
- Clear visibility into allowed transitions
- Prevents invalid state changes at runtime
- Easy to add new states without modifying existing code

**State Diagram:**
```
OPEN → INVESTIGATING → RESOLVED → CLOSED
  │         │              │          │
  └─────────┴──────────────┴──────────┘
```

### 4. Strategy Pattern

**Decision:** Use interchangeable alert strategies per component type.

**Rationale:**
- Different component types require different severity rules
- Easy to add new component types without modifying existing code
- Each strategy is self-contained and testable

**Strategies:**
- RDBMS → P0 (critical)
- QUEUE → P1 (high)
- CACHE → P2 (medium)
- API → P3 (default)
- NOSQL → P1 (high)

### 5. Rate Limiting

**Decision:** Token bucket algorithm using Redis + Lua script.

**Rationale:**
- Atomic - no race conditions
- Distributed - works across instances
- Burst handling - allows short spikes

**Configuration:**
- 1000 requests/second sustained
- 2000 requests burst capacity

---

## 📈 Scaling Strategy

### Horizontal Scaling

| Component | Scaling Approach |
|-----------|-------------------|
| **API** | Add more instances behind load balancer |
| **Kafka** | Increase partitions, add consumers |
| **Workers** | Add more consumer instances |
| **PostgreSQL** | Read replicas for queries |
| **Redis** | Redis Cluster for caching |

### Performance Targets

| Metric | Target |
|--------|--------|
| **Signal Ingestion** | 10,000 signals/sec |
| **API Latency** | < 50ms (p99) |
| **Incident Creation** | < 100ms |
| **Dashboard Load** | < 500ms |

### Capacity Planning

```
Signals/sec: 10,000
Kafka partitions: 6
Worker instances: 5 (concurrency 3 each)
Effective throughput: 15,000 signals/sec

PostgreSQL connections: 20 (pool)
MongoDB connections: 50
Redis connections: 16
```

---

## 🔧 Configuration

### Environment Variables

```yaml
# Database
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/incident_management
SPRING_DATASOURCE_USERNAME: ims_user
SPRING_DATASOURCE_PASSWORD: ims_password

# MongoDB
SPRING_DATA_MONGODB_URI: mongodb://ims_mongo:ims_mongo_password@mongo:27017/incident_management

# Redis
SPRING_DATA_REDIS_HOST: redis

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
```

### Custom Properties

```yaml
ims:
  debounce:
    window-seconds: 10      # Debounce window
    max-signals: 100       # Max signals before forcing incident
  rate-limit:
    requests-per-second: 1000
    burst-capacity: 2000
  worker:
    concurrency: 5
    poll-timeout: 1000
  metrics:
    throughput-log-interval-seconds: 5
```

---

## 📝 License

MIT License - see LICENSE file for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request