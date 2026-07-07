# 01. Business Requirements Document (BRD)

## 1 Document Information

| Item         | Value               |
| ------------ | ------------------- |
| Project      | File Upload Service |
| Author       | HaiNh               |
| Version      | 1.0                 |
| Status       | Draft               |
| Last Updated |                     |

---

# 2. Business Background

## 1 Background

Nhiều hệ thống cần cho phép người dùng lưu trữ và quản lý file.

Hiện tại chưa có một hệ thống tập trung để:

- Upload file.
- Lưu trữ file.
- Quản lý metadata.
- Tìm kiếm file.
- Download file.

Điều này gây khó khăn trong việc quản lý dữ liệu và mở rộng hệ thống.

---

## 2 Business Problem

Hệ thống cần giải quyết các vấn đề sau:

- Người dùng không có nơi lưu trữ file tập trung.
- Khó quản lý metadata.
- Khó tìm kiếm file.
- Không hỗ trợ upload file dung lượng lớn.
- Thiếu khả năng mở rộng.

---

# 3. Business Objectives

Mục tiêu của dự án:

- Cho phép upload file.
- Quản lý metadata.
- Download file.
- Delete file.
- Search file.
- Thiết kế hướng production-ready.
- Có khả năng mở rộng trong tương lai.

---

# 4. Stakeholders

| Stakeholder          | Responsibility                  |
| -------------------- | ------------------------------- |
| End User             | Upload và quản lý file          |
| Backend Developer    | Thiết kế và phát triển hệ thống |
| System Administrator | Quản lý hạ tầng                 |

---

# 5. Business Scope

## 1 In Scope

- Upload file.
- Download file.
- Delete file.
- List files.
- Search file.
- Metadata management.
- Object storage.
- User authentication.
- User authorization.

## 2 Out of Scope

- Payment.
- Public sharing.
- Complex folder permission
- Full user management system 
- Mobile application.
- CDN integration

---

# 6. Business Requirements

## 1 BR-01

Hệ thống phải cho phép người dùng upload file.

Priority: High

---

## 2 BR-02

Hệ thống phải lưu metadata của file.

Priority: High

---

## 3 BR-03

Hệ thống phải cho phép download file.

Priority: High

---

## 4 BR-04

Hệ thống phải cho phép xóa file.

Priority: High

---

## 5 BR-05

Hệ thống phải cho phép tìm kiếm file theo tên.

Priority: Medium

---

## 6 BR-06

Hệ thống cần hỗ trợ upload file dung lượng lớn.

Priority: High

---

## 7 BR-07

Hệ thống cần có khả năng mở rộng khi số lượng người dùng tăng.

Priority: Medium

---

# 7. Business Constraints

- Sử dụng Java và Spring Boot.
- Object Storage sử dụng MinIO.
- Metadata lưu trong MySQL.
- Triển khai bằng Docker Compose.

---

# 8. Assumptions

- Người dùng đã đăng nhập (Authentication không thuộc phạm vi dự án).
- Người dùng có quyền upload file của mình.
- Object Storage luôn sẵn sàng.
- Mạng ổn định trong điều kiện bình thường.

---

# 9. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Upload bị gián đoạn | Cao | Resume Upload |
| Metadata và Object không đồng bộ | Cao | Transaction Strategy |
| File quá lớn | Trung bình | Multipart Upload |
| Object Storage unavailable | Cao | Retry / Monitoring |

---

# 10. Success Criteria

Dự án được xem là thành công khi:

- Upload thành công.
- Download thành công.
- Delete thành công.
- Metadata được quản lý chính xác.
- Search hoạt động đúng.
- Có integration test.
- Có monitoring.
- Thiết kế đủ khả năng mở rộng.

---

# 11. Related Documents

- [[00-overview]]
- [[/docs/02-frd]]
- [[/docs/03-nfr]]
- [[04-hld]]
- [[05-erd]]