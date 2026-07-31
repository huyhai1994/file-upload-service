package org.mini_lab.file_upload_service.file_upload.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.shared.response.ApiResponse;
import org.mini_lab.file_upload_service.file_upload.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.file_upload.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.file_upload.service.upload.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @WithSpan("upload-controller")
    @PostMapping
    ResponseEntity<ApiResponse<FileMetadataResponseDTO>> upload(@ModelAttribute UploadRequestObjectDTO request) {
        FileMetadataResponseDTO fileMetadataResponseDTO = fileUploadService.processUploadFile(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(fileMetadataResponseDTO));
    }

}
