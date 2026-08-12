package org.mini_lab.file_upload_service.security.authentication.shared.repository;

import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByUsernameAndLockedUntilIsNotNull(String username);

    @Modifying
    @Query("""
                    update User u
                    set u.failedLoginCount = u.failedLoginCount + 1,
                                u.lockedUntil = (
                                            case 
                                                when u.failedLoginCount + 1 >= :maxAttemptCount
                                                 then :lockedUtil
                                                else u.lockedUntil
                                            end),
                                u.updatedAt = :now
                    where u.username = :username
            """)
    int recordLoginFailureCount(@Param("username") String username,
                                @Param("lockedUtil") LocalDateTime lockedUntil,
                                @Param("maxAttemptCount") Integer maxAttemptCounts,
                                @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
                    update User u
                    set u.failedLoginCount = 0 ,
                        u.lockedUntil  = null ,
                        u.updatedAt = :now
                    where u.username = :username
            """)
    int resetFailureCount(@Param("username") String username,
                          @Param("now") LocalDateTime now);

}
