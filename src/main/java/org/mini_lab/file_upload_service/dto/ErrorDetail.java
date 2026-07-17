package org.mini_lab.file_upload_service.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    private String message;
}
