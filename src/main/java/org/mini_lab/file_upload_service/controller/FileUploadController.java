package org.mini_lab.file_upload_service.controller;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.dto.file_upload.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.file_upload.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.service.upload.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping
    ResponseEntity<ApiResponse<FileMetadataResponseDTO>> upload(@ModelAttribute UploadRequestObjectDTO request) {
        FileMetadataResponseDTO fileMetadataResponseDTO = fileUploadService.processUploadFile(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(fileMetadataResponseDTO));
    }

}
