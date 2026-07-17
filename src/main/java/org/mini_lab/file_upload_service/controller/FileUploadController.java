package org.mini_lab.file_upload_service.controller;

import jakarta.servlet.annotation.MultipartConfig;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping
    ResponseEntity<FileMetadataResponseDTO> upload(UploadRequestObjectDTO request) {
        FileMetadataResponseDTO fileMetadataResponseDTO = fileUploadService.processUploadFile(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fileMetadataResponseDTO);
    }

}
