package org.mini_lab.file_upload_service.security.authentication.register.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.security.authentication.register.service.OutboxEventStateManager;
import org.mini_lab.file_upload_service.security.authentication.register.service.OutboxWorker;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageRelayMockTest {

    @InjectMocks
    MessageRelay messageRelay;

    @Mock
    OutBoxEventRepository outBoxEventRepository;

    @Mock
    OutboxWorker outboxWorker;

    @Mock
    OutboxEventStateManager outboxEventStateManager;

    @Captor
    ArgumentCaptor<Long> captor;

    @Test
    void polling_whenThereIsAnPendingJobClaimed_thenPublishEvent() {
        when(outBoxEventRepository.findByStatus(any())).thenReturn(List.of(1L));

        messageRelay.polling();

        inOrder(
                outBoxEventRepository,
                outboxEventStateManager,
                outboxWorker
        );

        verify(outboxEventStateManager).markProcessing(captor.capture());
        assertThat(captor.getValue()).isEqualTo(1L);
        verify(outboxWorker).publishEvent(eq(1L));

    }

    @Test
    void polling_whenInvalidTransitionExceptionThrown_thenSkipThatJob() {
        when(outBoxEventRepository.findByStatus(any())).thenReturn(List.of(1L, 2L));
        doThrow(InvalidStateTransitionException.class)
                .when(outboxEventStateManager).markProcessing(1L);
        messageRelay.polling();

        inOrder(
                outBoxEventRepository,
                outboxEventStateManager,
                outboxWorker
        );

        verify(outboxEventStateManager).markProcessing(2L);
        verify(outboxWorker).publishEvent(eq(2L));
        verify(outboxWorker, never()).publishEvent(eq(1L));
    }

    @Test
    void polling_whenExceptionThrown_thenSkipThatJobAndThrowInternalException() {
        //ARRANGE
        when(outBoxEventRepository.findByStatus(any())).thenReturn(List.of(1L, 2L));
        doThrow(RuntimeException.class)
                .when(outboxWorker).publishEvent(1L);

        //ACT
        messageRelay.polling();

        //VERIFY
        verify(outboxEventStateManager, times(1)).markProcessing(1L);
        verify(outboxWorker, times(1)).publishEvent(eq(1L));
        verify(outboxEventStateManager, times(1)).handlePublishFailure(1L);

        verify(outboxEventStateManager, times(1)).markProcessing(2L);
        verify(outboxWorker, times(1)).publishEvent(eq(2L));
    }

}