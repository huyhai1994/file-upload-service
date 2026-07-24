package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.component.FileValidator;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.service.validator.FileVerifyService;
import org.mockito.InOrder;

import java.util.List;

import static org.mockito.Mockito.*;

class FileRequestValidatorServiceMockTest {


    @Test
    void whenVerifyFile_thenCallValidatorsInAscendingOrder() {
        FileValidator validatorOrder1 = mock(FileValidator.class);
        FileValidator validatorOrder2 = mock(FileValidator.class);
        FileValidator validatorOrder3 = mock(FileValidator.class);

        when(validatorOrder1.order()).thenReturn(1);
        when(validatorOrder2.order()).thenReturn(2);
        when(validatorOrder3.order()).thenReturn(3);

        FileVerifyService service = new FileVerifyService(
                List.of(
                        validatorOrder3,
                        validatorOrder1,
                        validatorOrder2
                )
        );

        FileUploadCommand command = mock(FileUploadCommand.class);


        service.validate(command);

        InOrder inOrder = inOrder(
                validatorOrder1,
                validatorOrder2,
                validatorOrder3
        );

        inOrder.verify(validatorOrder1).validate(command);
        inOrder.verify(validatorOrder2).validate(command);
        inOrder.verify(validatorOrder3).validate(command);
    }
}
