package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.SecurityUser;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsManager implements UserDetailsService {
    private final NormalizeUsernameService normalizeUsernameService;
    private final UserFinderService userFinderService;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) {
        String normalizedUsername = normalizeUsernameService.normalizeUsername(username);
        User user = getRequiredUser(normalizedUsername);
        return new SecurityUser(user);
    }

    private User getRequiredUser(String username) {
        return userFinderService.find(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username
                ));
    }
}