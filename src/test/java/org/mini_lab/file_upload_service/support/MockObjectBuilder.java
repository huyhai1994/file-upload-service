package org.mini_lab.file_upload_service.support;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.file_upload.UploadObjectResult;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    public static @NotNull FileMetadata getValidUploadingFileMetadata() {
        FileMetadata fileMetadata = createFileMetadata();
        fileMetadata.setStatus(FileState.UPLOADING);
        return fileMetadata;
    }

    public static @NotNull FileMetadata getValidCompletedFileMetadata() {
        FileMetadata fileMetadata = createFileMetadata();
        fileMetadata.setStatus(FileState.COMPLETED);
        return fileMetadata;
    }

    public static @NotNull FileMetadata getValidDeletingFileMetadata() {
        FileMetadata fileMetadata = createFileMetadata();
        fileMetadata.setStatus(FileState.DELETING);
        return fileMetadata;
    }

    private static @NonNull FileMetadata createFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setTitle("Avatar of John Doe");
        fileMetadata.setFileName("avatar.png");
        fileMetadata.setContentType("image/png");
        fileMetadata.setExtension("png");
        fileMetadata.setObjectKey("2026/07/08/8b7c3d0d-f0d1-4a7c-a3a4-7d4ef2e81a55.png");
        fileMetadata.setSize(512_384L);
        fileMetadata.setChecksum("9d5ed678fe57bcca610140957afab571f6d9f1f5e53e7d8d0b8f359bd2d96d8e");
        return fileMetadata;
    }

    public static FileMetadata getNonValidUploadingFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setTitle("Avatar of John Doe");
        fileMetadata.setFileName(null);
        fileMetadata.setContentType(null);
        fileMetadata.setExtension("png");
        fileMetadata.setObjectKey(null);
        fileMetadata.setSize(512_384L);
        fileMetadata.setChecksum("9d5ed678fe57bcca610140957afab571f6d9f1f5e53e7d8d0b8f359bd2d96d8e");
        fileMetadata.setStatus(FileState.UPLOADING);
        fileMetadata.setStatus(null);
        return fileMetadata;
    }

    public static UploadObjectResult getUploadObjectResult() {
        return UploadObjectResult.builder()
                .etag(UUID.randomUUID().toString())
                .checksum(UUID.randomUUID().toString())
                .build();
    }
}
