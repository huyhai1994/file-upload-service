# File Upload Service

## 1 Overview

### 1.1 Problem

File Upload là một thành phần phổ biến trong nhiều hệ thống như Cloud Storage, CMS, ERP, CRM và các ứng dụng quản lý tài liệu.

Mục tiêu của dự án là xây dựng một File Upload Service cho phép người dùng tải lên, lưu trữ và quản lý file một cách an toàn, tin cậy và có khả năng mở rộng.

### 1.2 Goals

- Upload file lên Object Storage.
- Lưu metadata vào cơ sở dữ liệu.
- Download file.
- Xóa file.
- Liệt kê danh sách file.
- Tìm kiếm file theo tên hoặc từ khóa.
- Hỗ trợ upload file dung lượng lớn.
- Thiết kế theo hướng production-ready.

### 1.3 Target Users

- Cá nhân.
- Gia đình.
- Có thể mở rộng cho doanh nghiệp nhỏ.

## 2 Features

### 2.1 Phase 1 - Basic File Upload
- [ ] Upload file
- [ ] Download file
- [ ] Delete file
- [ ] List files
- [ ] Store file metadata
- [ ] Store object in MinIO

### 2.2 Phase 2 - Reliable Upload
- [ ] Multipart upload
- [ ] Resume upload
- [ ] Checksum validation
- [ ] Idempotency
- [ ] Upload session timeout

### 2.3 Phase 3 - Advanced Backend Features
- [ ] Presigned URL
- [ ] File versioning
- [ ] Quota management
- [ ] Virus scan
- [ ] Thumbnail generation
- [ ] Event-driven processing
- [ ] Outbox pattern
- [ ] Search - Elastic Search

---

## 3 Architecture

```text
Client
  |
  v
Spring Boot API
  |
  +--> MySQL / MariaDB
  |
  +--> MinIO Object Storage
```

Xem chi tiết tại: [[04-hld]]

---

## 4 Tech Stack

| Layer          | Technology                       |
| -------------- | -------------------------------- |
| Language       | Java                             |
| Framework      | Spring Boot                      |
| Database       | MySQL                            |
| ORM            | Spring Data JPA / Hibernate      |
| Object Storage | MinIO                            |
| Migration      | Flyway                           |
| Testing        | JUnit 5, Mockito, Testcontainers |
| Monitoring     | Prometheus, Grafana              |
| Deployment     | Docker Compose                   |

---

## 5 Documentation

| Document                         | Description                 |
|----------------------------------|-----------------------------|
| [[file-upload/docs/00-overview]] | Tổng quan dự án             |
| [[file-upload/docs/01-brd]]      | Business requirements       |
| [[file-upload/docs/02-frd]]      | Functional requirements     |
| [[file-upload/docs/03-nfr]]      | Non-functional requirements |
| [[04-hld]]                       | High level design           |
| [[05-erd]]                       | Entity relationship diagram |
| [[06-api-spec]]                  | API specification           |
| [[07-sequence-diagram]]          | Sequence diagrams           |
| [[08-state-machine]]             | Upload state machine        |
| [[09-lld]]                       | Low level design            |

---

## 6 Core Domain Concepts

### 6.1 File

Đại diện cho metadata của file đã upload.

### 6.2 Upload Session

Đại diện cho một phiên upload, đặc biệt quan trọng với multipart/resume upload.

### 6.3 Object Key

Đường dẫn định danh object trong MinIO.

### 6.4 File Status

Trạng thái vòng đời của file.

```text
INIT
  -> UPLOADING
  -> VERIFYING
  -> COMPLETED
  -> FAILED
  -> ABORTED
  -> EXPIRED
```

---

## 7 Engineering Highlights

- Transaction boundary design
- Metadata consistency
- Object storage integration
- Multipart upload
- Resume upload
- Idempotency
- Checksum validation
- State machine
- Retry strategy
- Integration testing with Testcontainers
- Monitoring with Prometheus/Grafana

---

## 8 Project Structure

```text
src/main/java/...
├── controller
├── application
├── domain
├── infrastructure
├── repository
├── config
└── exception
```

---

## 9 Getting Started

### 9.1 Start infrastructure

```bash
docker compose up -d
```

### 9.2 Run application

```bash
./mvnw bootRun
```

### 9.3 Run tests

```bash
./mvnw clean test
```

---

## 10 Configuration

Main configuration file:

```text
application.yml
```

Important configs:

- Server port
- Database connection
- MinIO endpoint
- MinIO bucket
- Multipart upload size
- Upload timeout
- Max file size

---

## 11 API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/files/upload` | Upload file |
| GET | `/api/files/{fileId}` | Get file metadata |
| GET | `/api/files/{fileId}/download` | Download file |
| DELETE | `/api/files/{fileId}` | Delete file |
| GET | `/api/files` | List files |

Chi tiết tại: [[06-api-spec]]

---

## 12 Testing Strategy

- Unit test service/domain logic
- Repository test with Testcontainers
- Integration test upload/download flow
- Failure case test
- Concurrent upload test
- Idempotency test

---

## 13 Monitoring

Metrics cần theo dõi:

- Upload success count
- Upload failure count
- Upload duration
- File size distribution
- Active upload sessions
- Storage error count
- Database query latency

---

## 14 Future Improvements

- Nginx reverse proxy
- Presigned upload URL
- Async virus scanning
- Thumbnail service
- Kafka event pipeline
- Outbox pattern
- CDN integration
- Rate limiting
- User quota
- Audit log

---

## 15 Status

Current phase:

> Phase 1 - Basic File Upload

Current focus:

- [ ] BRD
- [ ] FRD
- [ ] NFR
- [ ] HLD
- [ ] ERD