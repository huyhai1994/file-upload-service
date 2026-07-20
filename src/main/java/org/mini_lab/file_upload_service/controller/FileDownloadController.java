package org.mini_lab.file_upload_service.controller;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mini_lab.file_upload_service.dto.FileDownloadResource;
import org.mini_lab.file_upload_service.service.FileDownloadService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileDownloadController {

    private static final int BUFFER_SIZE = 16 * 1024;

    private final FileDownloadService fileDownloadService;

    @GetMapping("{fileId}/download")
    ResponseEntity<StreamingResponseBody> download(@PathVariable Long fileId) {
        FileDownloadResource resource = fileDownloadService.prepareDownload(fileId);
        StreamingResponseBody responseBody = getStreamingResponseBody(resource);

        return ResponseEntity.ok()
                .contentType(resolveMediaType(resource.contentType()))
                .contentLength(resource.size())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(
                                        resource.fileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .body(responseBody);

    }

    private static @NotNull StreamingResponseBody getStreamingResponseBody(FileDownloadResource resource) {
        return outputStream -> {
            try (InputStream inputStream =
                         resource.inputStreamSupplier().open()) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
            } catch (IOException exception) {
                throw exception;
            }
        };
    }

    private MediaType resolveMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}
