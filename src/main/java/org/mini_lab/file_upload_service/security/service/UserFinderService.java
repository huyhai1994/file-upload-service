package org.mini_lab.file_upload_service.security.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.entity.User;
import org.mini_lab.file_upload_service.security.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFinderService {
    private final UserRepository userRepository;

    public Optional<User> find(String username){
        return userRepository.findByUsername(username);
    }



}
