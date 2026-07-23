package org.mini_lab.file_upload_service.repository;

import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    @Modifying
    @Query("""
            update FileMetadata fm
            set fm.status = FileState.COMPLETED,
                fm.checksum = :checksum,
                fm.completedAt = :now
            where fm.id = :fileId
              and fm.status = FileState.UPLOADING
            """)
    int markCompletedIfUploading(@Param("fileId") Long fileId,
                                 @Param("checksum") String checksum, LocalDateTime now);

    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.FAILED,
                        fm.failedAt = :now
                    where fm.id = :fileId
                    and fm.status = FileState.UPLOADING
            """)
    int markFailedIfUploading(@Param("fileId") Long fileId,
                              @Param("now") LocalDateTime now);


    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.DELETING
                    where fm.id = :fileId
                    and fm.status = FileState.COMPLETED
            """)
    int markDeletingIfCompleted(@Param("fileId") Long fileId, LocalDateTime now);

    @Modifying
    @Query("""
                    update FileMetadata  fm
                    set fm.status = FileState.DELETED
                    where fm.id = :fileId
                    and fm.status = FileState.DELETING
            """)
    int markDeletedIfDeleting(@Param("fileId") Long fileId, LocalDateTime localDateTime);


    @Query("""
            select fm.id from FileMetadata fm
            where fm.status = :state
            and fm.deletingAt <= :cutOffTime
                        order by  fm.deletingAt
            """)
    List<Long> findTimedOutFiledIds(@Param("cutOffTime") LocalDateTime cutOffTime,
                                    @Param("state") FileState fileState,
                                    Pageable pageable);

    Optional<FileMetadata> getFileMetadataById(Long id);
}
