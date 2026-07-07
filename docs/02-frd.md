# 02. Functional Requirements Document (FRD)

## 1 Document Information

| Item         | Value               |
| ------------ | ------------------- |
| Project      | File Upload Service |
| Author       | HaiNh               |
| Version      | 1.0                 |
| Status       | Draft               |
| Last Updated |                     |

---

# 2. Purpose

Tài liệu này mô tả các chức năng mà hệ thống File Upload Service phải cung cấp.

Các yêu cầu trong tài liệu sẽ được sử dụng để thiết kế HLD, ERD, API và triển khai hệ thống.

---

# 3. Functional Overview

Hệ thống cung cấp các chức năng chính:

- Upload file
- Download file
- Delete file
- List files
- Search files
- Multipart upload
- Resume upload
- Metadata management

---

# 4. Functional Requirements

---

## 1 FR-01 Upload File

### 1.1 Description

Cho phép người dùng upload một file lên hệ thống.

### 1.2 Actor

User

### 1.3 Trigger

User chọn file và gửi yêu cầu upload.

### 1.4 Preconditions

- File hợp lệ.
- Kích thước không vượt quá giới hạn.

### 1.5 Main Flow

1. User gửi file.
2. Hệ thống validate request.
3. Upload object vào MinIO.
4. Lưu metadata vào MySQL.
5. Trả về thông tin file.

### 1.6 Alternative Flow

- File quá lớn.
- Upload thất bại.
- Metadata lưu thất bại.

### 1.7 Postconditions

- File tồn tại trong Object Storage.
- Metadata tồn tại trong Database.

---

## 2 FR-02 Download File

### 2.1 Description

Cho phép người dùng tải file.

### 2.2 Actor

User

### 2.3 Preconditions

- File tồn tại.

### 2.4 Main Flow

1. User gửi request.
2. Hệ thống tìm metadata.
3. Lấy object từ MinIO.
4. Trả dữ liệu về client.

### 2.5 Exception

- File không tồn tại.
- Object bị mất.

---

## 3 FR-03 Delete File

### 3.1 Description

Cho phép người dùng xóa file.

### 3.2 Main Flow

1. User gửi request.
2. Validate quyền.
3. Xóa object.
4. Xóa metadata.

---

## 4 FR-04 List Files

### 4.1 Description

Hiển thị danh sách file.

### 4.2 Main Flow

1. User gửi request.
2. Query metadata.
3. Phân trang.
4. Trả kết quả.

---

## 5 FR-05 Search File

### 5.1 Description

Tìm kiếm file theo tên hoặc từ khóa.

### 5.2 Main Flow

1. User nhập keyword.
2. Query metadata.
3. Trả danh sách phù hợp.

---

## 6 FR-06 Multipart Upload

### 6.1 Description

Cho phép upload file lớn theo nhiều phần.

### 6.2 Main Flow

1. Tạo Upload Session.
2. Upload Part.
3. Lưu trạng thái từng Part.
4. Complete Multipart Upload.
5. Verify checksum.

---

## 7 FR-07 Resume Upload

### 7.1 Description

Tiếp tục upload khi bị gián đoạn.

### 7.2 Main Flow

1. Client gửi Upload Session ID.
2. Hệ thống kiểm tra Part đã upload.
3. Client upload các Part còn thiếu.
4. Complete Upload.

---

## 8 FR-08 File Metadata

### 8.1 Description

Quản lý metadata của file.

Metadata bao gồm:

- File Name
- Object Key
- Size
- MIME Type
- Upload Time
- Checksum
- Owner

---

# 5. Business Rules

### 0.1 BR-01

Object chỉ được lưu một lần.

---

### 0.2 BR-02

Metadata chỉ được tạo khi upload object thành công.

---

### 0.3 BR-03

Không cho phép upload file vượt quá giới hạn.

---

### 0.4 BR-04

Object Key phải duy nhất.

---

### 0.5 BR-05

Mỗi Upload Session chỉ được Complete một lần.

---

# 6. Error Handling

| Code | Description |
|------|-------------|
| FILE_NOT_FOUND | File không tồn tại |
| INVALID_FILE | File không hợp lệ |
| FILE_TOO_LARGE | File vượt giới hạn |
| STORAGE_ERROR | Lỗi Object Storage |
| CHECKSUM_FAILED | Sai checksum |
| UPLOAD_SESSION_EXPIRED | Upload Session hết hạn |

---

# 7. Functional Dependencies

| Requirement | Depends On |
|------------|------------|
| Download | Upload |
| Delete | Upload |
| Search | Metadata |
| Multipart Upload | Upload Session |
| Resume Upload | Multipart Upload |

---

# 8. Acceptance Criteria

### 0.1 Upload

- Có thể upload thành công.
- Metadata được lưu.
- Object được lưu.
- Trả về HTTP Status đúng.

---

### 0.2 Download

- Download đúng nội dung.
- Trả đúng MIME Type.

---

### 0.3 Delete

- Metadata bị xóa.
- Object bị xóa.

---

### 0.4 Search

- Có thể tìm theo tên.
- Có phân trang.

---

### 0.5 Multipart Upload

- Upload file lớn thành công.
- Có thể resume.
- Verify checksum.

---

# 9. Related Documents

- [[01-brd]]
- [[/docs/03-nfr]]
- [[04-hld]]
- [[05-erd]]
- [[06-api-design]]