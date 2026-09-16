package org.mini_lab.file_upload_service.security.notification.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockOutboxEventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutBoxEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutBoxEventRepository outBoxEventRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void save_whenSaveEvent_thenSuccess() {
        OutboxEvent event = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.pendingEvent());
        entityManager.clear();
        OutboxEvent persistedEvent = outBoxEventRepository.findAll().get(0);
        assertThat(persistedEvent.getId()).isEqualTo(MockOutboxEventBuilder.pendingEvent().getId());
    }

}