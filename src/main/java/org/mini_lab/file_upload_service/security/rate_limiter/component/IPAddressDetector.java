package org.mini_lab.file_upload_service.security.rate_limiter.component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IPAddressDetector {
    public String detect(HttpServletRequest request) {
        String ipAddress = request.getLocalAddr();
        log.info(
                "Login rate-limit identity: remoteAddr={}, detectedIp={}",
                request.getRemoteAddr(),
                ipAddress
        );
        return ipAddress;
    }
}
