# 00. Project Overview

## 1 Project Name

File Upload Service

---

## 2 Summary

File Upload Service là hệ thống cho phép người dùng upload, lưu trữ, quản lý và tải xuống file cá nhân.

Hệ thống sử dụng Spring Boot để xử lý API, MySQL để lưu metadata và MinIO để lưu nội dung file dưới dạng object storage.

Mục tiêu của dự án là xây dựng một backend service có thiết kế gần production, có khả năng mở rộng, kiểm soát consistency giữa database và object storage, đồng thời hỗ trợ các tính năng nâng cao như multipart upload, resume upload, checksum validation và tìm kiếm file.

---

## 3 Background

Trong nhiều hệ thống thực tế như cloud storage, hệ thống tài liệu nội bộ, CMS, CRM, ERP hoặc ứng dụng gia đình, nhu cầu upload và quản lý file là rất phổ biến.

Tuy nhiên, xử lý file upload không chỉ là nhận file rồi lưu vào disk. Một hệ thống upload tốt cần giải quyết nhiều vấn đề backend quan trọng:

- Lưu metadata và binary file tách biệt.
- Đảm bảo consistency giữa database và object storage.
- Hỗ trợ file dung lượng lớn.
- Xử lý lỗi khi upload thất bại.
- Kiểm tra checksum.
- Quản lý trạng thái upload.
- Có khả năng mở rộng khi số lượng người dùng tăng.

---

## 4 Problem Statement

Người dùng cần một hệ thống tập trung để:

- Upload file cá nhân.
- Lưu trữ file an toàn.
- Xem danh sách file đã upload.
- Tìm kiếm file theo từ khóa.
- Download file khi cần.
- Xóa file không còn sử dụng.

---

## 5 Project Goals

### 5.1 Phase 1

- Upload file lên MinIO.
- Lưu metadata vào MySQL.
- Download file.
- Delete file.
- List files.
- Basic search theo file name.

### 5.2 Phase 2

- Multipart upload.
- Resume upload.
- Checksum validation.
- Idempotency.
- Upload session timeout.

### 5.3 Phase 3

- Presigned URL.
- Quota management.
- File versioning.
- Virus scan.
- Thumbnail generation.
- Elasticsearch search.
- Event-driven processing.
- Outbox pattern.

---

## 6 Target Users

- Cá nhân.
- Thành viên trong gia đình.
- Có thể mở rộng cho nhóm nhỏ hoặc doanh nghiệp nhỏ.

---

## 7 System Scope

### 7.1 In Scope

- File upload.
- File download.
- File deletion.
- File listing.
- File metadata management.
- Object storage integration.
- Basic search.
- Multipart upload.
- Upload state management.
- Monitoring.

### 7.2 Out of Scope - Initial Version

- Payment.
- Public file sharing.
- Complex folder permission.
- Real-time collaboration.
- Full user management system.
- CDN integration.
- Mobile application.

---

## 8 High Level Architecture Summary

```text
Client
  |
  v
Spring Boot REST API
  |
  +--> MySQL Metadata Database
  |
  +--> MinIO Object Storage