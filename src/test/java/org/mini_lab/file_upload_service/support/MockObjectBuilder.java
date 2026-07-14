package org.mini_lab.file_upload_service.support;

import org.jetbrains.annotations.NotNull;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

public class MockObjectBuilder {
    public static @NotNull FileUploadCommand getFileUploadCommand(MultipartFile file) {
        return FileUploadCommand.builder()
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .file(file)
                .build();
    }

    public static @NotNull MockMultipartFile getEmptyMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );
    }

    public static @NotNull MockMultipartFile getTextContentTypeMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello MinIO".getBytes(StandardCharsets.UTF_8)
        );
    }

    public static @NotNull MockMultipartFile getNonValidExtensionMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.sh",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    public static @NotNull MockMultipartFile getEmptyFilenameMultipartFile() {
        return new MockMultipartFile(
                "file",
                "",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    public static @NotNull MockMultipartFile getMismatchMimeMultipartFile() {
        byte[] pngHeader = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };

        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                pngHeader
        );
    }
}
