package org.mini_lab.file_upload_service.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NormalizeUsernameService {

    public String normalizeUsername(String username) {
        return
                username
                        .trim()
                        .toLowerCase(Locale.ROOT);
    }
}
