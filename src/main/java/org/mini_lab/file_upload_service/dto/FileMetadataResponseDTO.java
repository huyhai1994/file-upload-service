package org.mini_lab.file_upload_service.dto;

import lombok.Builder;
import org.mini_lab.file_upload_service.entity.FileState;

@Builder
public record FileMetadataResponseDTO(Long fileId, String fileName, Long size, String contentType, String checksum,
                                      FileState state) {
}
