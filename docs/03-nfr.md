# Non-Functional Requirements (NFR)

## 1 Document Information

| Item | Value |
|------|-------|
| Project | File Upload Service |
| Author | HaiNh |
| Version | 1.0 |
| Status | Draft |
| Last Updated | |

---

# Purpose

Tài liệu này mô tả các yêu cầu phi chức năng của hệ thống.

Các yêu cầu này sẽ được sử dụng làm cơ sở thiết kế kiến trúc (HLD), lựa chọn công nghệ và triển khai hệ thống.

---

# Performance

## 1 NFR-01 Response Time

- API không upload/download:
    - P95 < 300 ms

- Upload API:
    - Thời gian phản hồi phụ thuộc vào kích thước file.

---

## 2 NFR-02 File Size

- Maximum upload size:
    - 5 GB

- Future:
    - 20 GB

---

## 3 NFR-03 Concurrent Upload

Hệ thống hỗ trợ:

- 100 concurrent uploads (Phase 1)

Có thể mở rộng trong tương lai.

---

# Scalability

## 1 NFR-04 Horizontal Scaling

Application phải có khả năng scale ngang.

Có thể triển khai nhiều instance phía sau Load Balancer.

---

## 2 NFR-05 User Growth

Thiết kế hướng tới:

- 10,000 users

không cần thay đổi kiến trúc chính.

---

## 3 NFR-06 Storage Growth

Object Storage phải có khả năng mở rộng khi dung lượng tăng.

Binary file không lưu trong MySQL.

---

# Availability

## 1 NFR-07 Service Availability

Mục tiêu:

- 99.9%

---

## 2 NFR-08 Failure Recovery

Nếu upload thất bại:

- Metadata không được tạo.
- Upload Session có thể resume.

---

# Reliability

## 1 NFR-09 Data Consistency

Metadata và Object Storage phải nhất quán.

Không được tồn tại metadata không có object.

---

## 2 NFR-10 Idempotency

Retry cùng một request không được tạo nhiều object.

---

## 3 NFR-11 Checksum Validation

Sau upload phải kiểm tra checksum.

---

# Security

## 1 NFR-12 Authentication

Không thuộc phạm vi Phase 1.

Có khả năng tích hợp JWT trong tương lai.

---

## 2 NFR-13 Authorization

User chỉ được thao tác trên file của mình.

---

## 3 NFR-14 Object Storage

Không expose trực tiếp MinIO.

Tất cả truy cập thông qua Backend API hoặc Presigned URL.

---

# Maintainability

## 1 NFR-15 Code Structure

Áp dụng kiến trúc phân lớp.

Ví dụ:

- Controller
- Application
- Domain
- Repository
- Infrastructure

---

## 2 NFR-16 Documentation

Hệ thống phải có:

- BRD
- FRD
- NFR
- HLD
- ERD
- API Design
- LLD

---

# Observability

## 1 NFR-17 Logging

Log các sự kiện:

- Upload Started
- Upload Completed
- Upload Failed
- Delete
- Download

---

## 2 NFR-18 Metrics

Thu thập:

- Upload count
- Upload duration
- Error rate
- Active upload
- Storage latency

---

## 3 NFR-19 Monitoring

Hệ thống tích hợp:

- Prometheus
- Grafana

---

# Testing

## 1 NFR-20 Unit Test

Business logic phải có Unit Test.

---

## 2 NFR-21 Integration Test

Các thành phần tích hợp:

- MySQL
- MinIO

được kiểm thử bằng Testcontainers.

---

## 3 NFR-22 Concurrent Test

Kiểm thử upload đồng thời.

---

# Deployment

## 1 NFR-23 Environment

Hỗ trợ:

- Local
- Docker Compose

Có khả năng mở rộng lên Kubernetes.

---

## 2 NFR-24 Configuration

Toàn bộ cấu hình nằm trong:

- `application.yml`

---
# Backup & Retention
## 1 NFR-25 File Backup Policy

File sau khi được lưu trữ quá 30 ngày phải được đưa vào quy trình backup.
  
Mục tiêu:  
  
- Giảm rủi ro mất dữ liệu.  
- Đảm bảo file quan trọng có bản sao dự phòng.  
- Tách dữ liệu đang dùng thường xuyên và dữ liệu lâu ngày.
## 2 NFR-26 Backup Frequency  
  
Hệ thống cần kiểm tra file cần backup theo lịch định kỳ.  
  
Ví dụ:  
  
- Daily backup scan  
- Backup các file có `created_at` hoặc `uploaded_at` quá 30 ngày  
- Không backup lại file đã được backup thành công  
  
## 3 NFR-27 Backup Reliability  
  
Backup phải đảm bảo:  
  
- Không làm mất file gốc.  
- Có trạng thái backup rõ ràng.  
- Có thể retry khi backup thất bại.  
- Có log và metrics cho backup process.


# Archive Policy

  
Trong các phase sau, những file được archive có thể bị xóa khỏi active Object Storage sau khi đã được backup thành công sang cold storage.  
  
Object gốc chỉ được xóa khi thỏa mãn đầy đủ các điều kiện:  
  
- Backup sang cold storage thành công.  
- Checksum verification thành công.  
- Metadata được cập nhật thành công.  
- Trạng thái file được chuyển thành `ARCHIVED`.  
- Backup object key đã được lưu lại.
# Capacity Planning

## 1 Expected Users

- Phase 1: 100 users
- Phase 2: 1,000 users
- Target: 10,000 users

## 2 Expected Storage

- Average file size: ...
- Max file size: 5 GB
- Daily uploads: ...
- Estimated storage growth: ... GB/month

## 3 Concurrent Upload

- Initial target: 100 concurrent uploads

## 4 Storage Backend

- Object Storage (MinIO)

---

# Constraints

- Java 21
- Spring Boot
- MySQL
- MinIO
- Docker Compose
- Flyway

---

# Future Considerations

Trong tương lai hệ thống có thể bổ sung:

- Redis
- Kafka
- Elasticsearch
- Virus Scan
- Thumbnail Service
- CDN
- Rate Limiter
- Presigned Upload
- Object Versioning

---

# Traceability

| NFR         | Related HLD |
| ----------- | ----------- |
| Scalability | [[04-hld]]  |
| Performance | [[04-hld]]  |
| Reliability | [[04-hld]]  |
| Security    | [[04-hld]]  |
| Monitoring  | [[04-hld]]  |