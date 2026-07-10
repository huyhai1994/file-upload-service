package org.mini_lab.file_upload_service.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UUIDObjectKeyGenerator implements ObjectKeyGenerator {

    private final Clock clock;

    @Override
    public String generate() {
        LocalDate date = LocalDate.now(clock);

        return "files/%d/%02d/%s".formatted(
                date.getYear(),
                date.getMonthValue(),
                UUID.randomUUID()
        );
    }
}
