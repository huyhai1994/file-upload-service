package org.mini_lab.file_upload_service.repository;

import org.mini_lab.file_upload_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
}
