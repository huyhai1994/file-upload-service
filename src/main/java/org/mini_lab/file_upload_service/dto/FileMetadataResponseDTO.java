package org.mini_lab.file_upload_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.mini_lab.file_upload_service.entity.FileState;

import java.time.Instant;

@Builder
public record FileMetadataResponseDTO(
        @JsonProperty("id")
        Long fileId,
        String title,
        String fileName,
        String contentType,
        String extension,
        Long size,
        String checksum,
        @JsonProperty("status")
        FileState state,
        Instant createdAt
) {
}
