package org.mini_lab.file_upload_service.service.extractor;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.mini_lab.file_upload_service.exception.FileReadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TikaMimeTypeExtractor implements MimeTypeExtractor {

    private final Tika tika;

    @Override
    public String extract(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new FileReadException(e);
        }
    }
}
