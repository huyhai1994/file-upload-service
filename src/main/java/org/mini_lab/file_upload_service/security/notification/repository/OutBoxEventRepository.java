package org.mini_lab.file_upload_service.security.notification.repository;

import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    // @TODO: PENDING -> PROCESSING
    // @TODO: PROCESSING -> PENDING & update retry_count + 1
    // @TODO: PROCESSING -> FAILED
    // @TODO: PROCESSING -> COMPLETED
}
