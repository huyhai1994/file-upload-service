package org.mini_lab.file_upload_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.enums.ErrorCode;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.FileUploadService;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileDownloadControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @BeforeEach
    void cleanUp() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void downloadFile_whenFileMetadataDoesNotExist_thenReturnNotFound()
            throws Exception {

        Long nonExistingId = 999_999L;

        mockMvc.perform(
                        get(
                                "/api/v1/files/{fileId}/download",
                                nonExistingId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.FILE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));
    }

    @Test
    void downloadFile_whenStateIsNotCompleted_thenReturnConflict() throws Exception {

        FileMetadata fileMetadata = MockObjectBuilder.getValidUploadingFileMetadata();
        FileMetadata persistedMetadata = fileMetadataRepository.save(fileMetadata);
        Long notReadyFileId = persistedMetadata.getId();
        mockMvc.perform(
                        get(
                                "/api/v1/files/{fileId}/download",
                                notReadyFileId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.FILE_NOT_AVAILABLE.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.FILE_NOT_AVAILABLE.getDefaultMessage()));

    }


    @Test
    void whenDownloadFileSuccess_shouldGetFullContent() throws Exception {
        MockMultipartFile multipartFile = getTextContentTypeMultipartFile();

        FileMetadataResponseDTO uploadedFile =
                fileUploadService.processUploadFile(
                        new UploadRequestObjectDTO(
                                multipartFile,
                                "hello"
                        )
                );

        MvcResult asyncResult = mockMvc.perform(
                        get(
                                "/api/v1/files/{fileId}/download",
                                uploadedFile.fileId()
                        )
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes(multipartFile.getBytes()))
                .andExpect(header().longValue(
                        HttpHeaders.CONTENT_LENGTH,
                        multipartFile.getSize()
                ))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment")
                ))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString(multipartFile.getOriginalFilename())
                ));
    }

}