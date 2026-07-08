package org.mini_lab.file_upload_service.repository;

import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {


}
