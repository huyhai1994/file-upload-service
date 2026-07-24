package org.mini_lab.file_upload_service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.service.retry.FileDeleteRetryService;
import org.mini_lab.file_upload_service.service.retry.TimedOutFileFinder;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeleteRetrySchedulerTest {

    @Mock
    private TimedOutFileFinder timedOutFileFinder;

    @Mock
    private FileDeleteRetryService fileDeleteRetryService;

    @InjectMocks
    private FileDeleteRetryScheduler scheduler;

    @Test
    void retryTimedOutDeletingFiles_whenTimedOutFilesExist_thenRetryEachFile() {
        // given
        when(timedOutFileFinder.findTimedOutFileIds()).thenReturn(List.of(1L, 2L, 3L));

        // when
        scheduler.retryTimedOutDeletingFiles();

        // then
        verify(timedOutFileFinder).findTimedOutFileIds();

        verify(fileDeleteRetryService).retryTimedOutFile(1L);
        verify(fileDeleteRetryService).retryTimedOutFile(2L);
        verify(fileDeleteRetryService).retryTimedOutFile(3L);

        verifyNoMoreInteractions(timedOutFileFinder, fileDeleteRetryService);
    }

    @Test
    void retryTimedOutDeletingFiles_whenNoTimedOutFileExists_thenDoNotRetry() {
        // given
        when(timedOutFileFinder.findTimedOutFileIds()).thenReturn(List.of());

        // when
        scheduler.retryTimedOutDeletingFiles();

        // then
        verify(timedOutFileFinder).findTimedOutFileIds();
        verifyNoInteractions(fileDeleteRetryService);
    }

    @Test
    void retryTimedOutDeletingFiles_whenOneFileFails_thenContinueRetryingRemainingFiles() {
        // given
        when(timedOutFileFinder.findTimedOutFileIds()).thenReturn(List.of(1L, 2L, 3L));

        doThrow(new RuntimeException("Database unavailable")).when(fileDeleteRetryService).retryTimedOutFile(2L);

        // when / then
        assertThatCode(scheduler::retryTimedOutDeletingFiles).doesNotThrowAnyException();

        verify(fileDeleteRetryService).retryTimedOutFile(1L);
        verify(fileDeleteRetryService).retryTimedOutFile(2L);
        verify(fileDeleteRetryService).retryTimedOutFile(3L);
    }

    @Test
    void retryTimedOutDeletingFiles_thenProcessFilesInFinderOrder() {
        // given
        when(timedOutFileFinder.findTimedOutFileIds()).thenReturn(List.of(10L, 20L, 30L));

        // when
        scheduler.retryTimedOutDeletingFiles();

        // then
        InOrder inOrder = inOrder(timedOutFileFinder, fileDeleteRetryService);

        inOrder.verify(timedOutFileFinder).findTimedOutFileIds();

        inOrder.verify(fileDeleteRetryService).retryTimedOutFile(10L);

        inOrder.verify(fileDeleteRetryService).retryTimedOutFile(20L);

        inOrder.verify(fileDeleteRetryService).retryTimedOutFile(30L);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void retryTimedOutDeletingFiles_whenFinderFails_thenPropagateException() {
        // given
        RuntimeException finderException = new RuntimeException("Failed to query timed-out files");

        when(timedOutFileFinder.findTimedOutFileIds()).thenThrow(finderException);

        // when / then
        assertThatThrownBy(scheduler::retryTimedOutDeletingFiles).isSameAs(finderException);

        verify(timedOutFileFinder).findTimedOutFileIds();
        verifyNoInteractions(fileDeleteRetryService);
    }
}
