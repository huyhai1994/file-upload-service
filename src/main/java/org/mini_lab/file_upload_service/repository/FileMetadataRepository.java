package org.mini_lab.file_upload_service.repository;

import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    @Modifying
    @Query("""
            update FileMetadata fm
            set fm.status = FileState.COMPLETED,
                fm.checksum = :checksum
            where fm.id = :fileId
              and fm.status = FileState.UPLOADING
            """)
    int markCompletedIfUploading(@Param("fileId") Long fileId,
                                 @Param("checksum") String checksum);

    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.FAILED
                    where fm.id = :fileId
                    and fm.status = FileState.UPLOADING
            """)
    int markFailedIfUploading(@Param("fileId") Long fileId);


    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.DELETING
                    where fm.id = :fileId
                    and fm.status = FileState.COMPLETED
            """)
    int markDeletingIfCompleted(@Param("fileId") Long fileId);

    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.DELETED
                    where fm.id = :fileId
                    and fm.status = FileState.DELETING
            """)
    int markDeletedIfDeleting(@Param("fileId") Long fileId);

    Optional<FileMetadata> getFileMetadataById(Long id);
}
