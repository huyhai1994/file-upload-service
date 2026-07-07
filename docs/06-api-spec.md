# API Design Principles

- API được thiết kế xoay quanh **resource** của hệ thống, ví dụ: `files`, `users`, `folders`.
- URL dùng **danh từ số nhiều**, không dùng động từ.
- HTTP Method thể hiện hành động:
  - `GET` đọc dữ liệu
  - `POST` tạo mới
  - `PATCH/PUT` cập nhật
  - `DELETE` xóa
- Response format phải nhất quán giữa các API.
- Error response phải có format chuẩn để client xử lý dễ dàng.
- API cần có versioning, ví dụ `/api/v1`, để tránh phá vỡ client khi API thay đổi.
- List API phải hỗ trợ pagination, sorting và filtering.
- Download file trả về binary stream, không bọc JSON.


# API Design

## 1 Document Information

| Item         | Value               |
| ------------ | ------------------- |
| Project      | File Upload Service |
| Author       | HaiNh               |
| Version      | 1.0                 |
| Status       | Draft               |
| Last Updated |                     |

---

# Purpose

Tài liệu này mô tả REST API của File Upload Service.

API được dùng để:

- Upload file
- Download file
- Xem metadata
- Xóa file
- Liệt kê file
- Tìm kiếm file

---

# API Conventions

## 1 Base URL

```text
/api/v1
```

## 2 Content Type

```text
application/json
multipart/form-data
application/octet-stream
```

## 3 Date Time Format

```text
ISO-8601
```

Ví dụ:

```text
2026-07-06T10:30:00+07:00
```

## 4 Common Response Format

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## 5 Common Error Response

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FILE_NOT_FOUND",
    "message": "File not found"
  }
}
```

---

# APIs

## 1 Upload File

### 1.1 Endpoint

```http
POST /api/v1/files
```

### 1.2 Content Type

```text
multipart/form-data
```

### 1.3 Request Parts

| Name  | Type   | Required | Description     |
| ----- | ------ | -------- | --------------- |
| file  | File   | Yes      | File cần upload |
| title | String | No       | Tiêu đề file    |

### 1.4 Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "My document",
    "fileName": "document.pdf",
    "contentType": "application/pdf",
    "extension": "pdf",
    "size": 1048576,
    "checksum": "sha256...",
    "status": "COMPLETED",
    "createdAt": "2026-07-06T10:30:00+07:00"
  },
  "error": null
}
```

### 1.5 Status Codes

| Status | Meaning |
|--------|---------|
| 201 | Upload thành công |
| 400 | File không hợp lệ |
| 413 | File quá lớn |
| 500 | Lỗi hệ thống |

---

## 2 Get File Metadata

### 2.1 Endpoint

```http
GET /api/v1/files/{fileId}
```

### 2.2 Path Variables

| Name | Type | Required | Description |
|------|------|----------|-------------|
| fileId | Long | Yes | ID của file |

### 2.3 Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "My document",
    "fileName": "document.pdf",
    "contentType": "application/pdf",
    "extension": "pdf",
    "size": 1048576,
    "checksum": "sha256...",
    "status": "COMPLETED",
    "createdAt": "2026-07-06T10:30:00+07:00",
    "updatedAt": "2026-07-06T10:30:00+07:00"
  },
  "error": null
}
```

---

## 3 Download File

### 3.1 Endpoint

```http
GET /api/v1/files/{fileId}/download
```

### 3.2 Path Variables

| Name | Type | Required | Description |
|------|------|----------|-------------|
| fileId | Long | Yes | ID của file |

### 3.3 Response

```text
Binary stream
```

### 3.4 Response Headers

| Header | Description |
|--------|-------------|
| Content-Type | MIME type của file |
| Content-Disposition | attachment filename |
| Content-Length | Kích thước file |

---

## 4 Delete File

### 4.1 Endpoint

```http
DELETE /api/v1/files/{fileId}
```

### 4.2 Path Variables

| Name | Type | Required | Description |
|------|------|----------|-------------|
| fileId | Long | Yes | ID của file |

### 4.3 Response

```json
{
  "success": true,
  "data": {
    "deleted": true
  },
  "error": null
}
```

---

## 5 List Files

### 5.1 Endpoint

```http
GET /api/v1/files
```

### 5.2 Query Parameters

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| page | Integer | No | 0 | Trang hiện tại |
| size | Integer | No | 20 | Số item mỗi trang |
| sort | String | No | createdAt,desc | Sắp xếp |

### 5.3 Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "My document",
        "fileName": "document.pdf",
        "contentType": "application/pdf",
        "extension": "pdf",
        "size": 1048576,
        "status": "COMPLETED",
        "createdAt": "2026-07-06T10:30:00+07:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "error": null
}
```

---

## 6 Search Files

### 6.1 Endpoint

```http
GET /api/v1/files/search
```

### 6.2 Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| keyword | String | Yes | Từ khóa tìm kiếm |
| page | Integer | No | Trang hiện tại |
| size | Integer | No | Số item mỗi trang |

### 6.3 Response

Dùng cùng response format với `List Files`.

---

# Error Codes

| Code                  | HTTP Status | Description            |
| --------------------- | ----------- | ---------------------- |
| INVALID_FILE          | 400         | File không hợp lệ      |
| FILE_TOO_LARGE        | 413         | File vượt quá giới hạn |
| FILE_NOT_FOUND        | 404         | Không tìm thấy file    |
| STORAGE_ERROR         | 500         | Lỗi Object Storage     |
| METADATA_SAVE_FAILED  | 500         | Lưu metadata thất bại  |
| CHECKSUM_FAILED       | 422         | Checksum không hợp lệ  |
| INTERNAL_SERVER_ERROR | 500         | Lỗi hệ thống           |
|                       |             |                        |

---

# Notes

- Phase 1 upload trực tiếp qua Backend API.
- Phase 2 có thể bổ sung Multipart Upload API.
- Phase 3 có thể bổ sung Presigned URL API.
