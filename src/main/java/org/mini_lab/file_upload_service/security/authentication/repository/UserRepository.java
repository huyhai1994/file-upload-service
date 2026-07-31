package org.mini_lab.file_upload_service.security.authentication.repository;

import org.mini_lab.file_upload_service.security.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String concurrentuser);
}
