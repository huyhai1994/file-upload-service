package org.mini_lab.file_upload_service.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.dto.FileDeleteResponseDTO;
import org.mini_lab.file_upload_service.service.delete.FileDeleteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileDeleteController {

    private final FileDeleteService fileDeleteService;

    @DeleteMapping("/{fileId}")
    ResponseEntity<ApiResponse<FileDeleteResponseDTO>> delete(@NonNull @PathVariable Long fileId) {
        FileDeleteResponseDTO body = fileDeleteService.processDeleteFile(fileId);
        return ResponseEntity
                .ok(ApiResponse.success(body));
    }
}
