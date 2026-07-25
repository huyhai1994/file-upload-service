package org.mini_lab.file_upload_service.component;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExtensionExtractor {

    @WithSpan("extension-extractor")
    public Optional<String> extract(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return Optional.empty();
        }

        return Optional.of(
                fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT)
        );
    }

}
