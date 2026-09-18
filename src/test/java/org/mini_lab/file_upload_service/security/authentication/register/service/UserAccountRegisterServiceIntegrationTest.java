package org.mini_lab.file_upload_service.security.authentication.register.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.notification.dto.OutboxEventStatus;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.VALID_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.VALID_EMAIL;

@SpringBootTest
@ActiveProfiles("test")
class UserAccountRegisterServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UserAccountRegisterService userAccountRegisterService;

    @Autowired
    UserRepository userRepository;

    @MockitoSpyBean
    OutBoxEventRepository outBoxEventRepository;

    @BeforeEach
    void cleanUp() {
        outBoxEventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void save_whenUserAccountSaved_thenOutboxEventSaved() {
        RegisterRequest request = validRegisterRequest();

        userAccountRegisterService.register(request);

        List<OutboxEvent> events = outBoxEventRepository.findAll();
        assertThat(userRepository.findAll().get(0).getEmailAddress()).isEqualTo(request.emailAddress());
        assertThat((long) events.size()).isOne();
        assertThat(events.get(0).getPayload()).isNotNull();
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(events.get(0).getCreatedAt()).isNotNull();
    }


    @Test
    void register_whenSavingOutboxEventFails_thenRollbackUser() {

        RegisterRequest request = validRegisterRequest();

        doThrow(new RuntimeException("Outbox database error"))
                .when(outBoxEventRepository)
                .save(any(OutboxEvent.class));

        assertThatThrownBy(() ->
                userAccountRegisterService.register(request)
        )
                .isInstanceOf(RuntimeException.class);

        assertThat(userRepository.findAll()).isEmpty();
        assertThat(outBoxEventRepository.findAll()).isEmpty();
    }

    private RegisterRequest validRegisterRequest() {
        return registerRequest(
                DEFAULT_USERNAME,
                VALID_PASSWORD,
                VALID_EMAIL
        );
    }

    private RegisterRequest registerRequest(
            String username,
            String password,
            String email
    ) {
        return new RegisterRequest(username, password, email);
    }
}