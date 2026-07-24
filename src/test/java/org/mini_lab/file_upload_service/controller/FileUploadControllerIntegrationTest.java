package org.mini_lab.file_upload_service.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.ApiError;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.dto.file_upload.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.mini_lab.file_upload_service.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileUploadControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String UPLOAD_URL = "/api/v1/files";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private JacksonUtils jacksonUtils;

    @BeforeEach
    void cleanUp() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void uploadFile_whenFileIsEmpty_thenReturnBadRequest() throws Exception {
        MockMultipartFile file =
                MockObjectBuilder.getEmptyMultipartFile();

        ApiError error = performBadRequest(file);

        assertApiError(error, ErrorCode.EMPTY_FILE);
    }

    @Test
    void uploadFile_whenFileNameInvalid_thenReturnBadRequest()
            throws Exception {

        MockMultipartFile file =
                MockObjectBuilder.getEmptyFilenameMultipartFile();

        ApiError error = performBadRequest(file);

        assertApiError(error, ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    void uploadFile_whenFileContentAndExtensionMismatch_thenReturnBadRequest()
            throws Exception {

        MockMultipartFile file =
                MockObjectBuilder.getMismatchMimeMultipartFile();

        ApiError error = performBadRequest(file);

        assertApiError(error, ErrorCode.INVALID_MIME_TYPE);
    }

    @Test
    void uploadFile_whenFileExtensionInvalid_thenReturnBadRequest()
            throws Exception {

        MockMultipartFile file =
                MockObjectBuilder.getNonValidExtensionMultipartFile();

        ApiError error = performBadRequest(file);

        assertApiError(error, ErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    void uploadFile_whenFileIsValid_thenReturnOk() throws Exception {
        MockMultipartFile file =
                MockObjectBuilder.getTextContentTypeMultipartFile();

        MvcResult result = mockMvc.perform(
                        multipart(UPLOAD_URL)
                                .file(file).param("title","hello")
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        ApiResponse<FileMetadataResponseDTO> response =
                jacksonUtils.convertFromJson(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assertThat(response.success()).isTrue();
        assertThat(response.error()).isNull();
        assertThat(response.data()).isNotNull();

        FileMetadataResponseDTO data = response.data();

        assertThat(data.fileId()).isNotNull();
        assertThat(data.fileName()).isNotNull();
        assertThat(data.contentType()).isNotNull();
        assertThat(data.size()).isNotNull();
        assertThat(data.checksum()).isNotNull();
        assertThat(data.state()).isEqualTo(FileState.COMPLETED);
        assertThat(data.createdAt()).isNotNull();
        assertThat(data.title()).isNotNull();
    }

    private ApiError performBadRequest(
            MockMultipartFile file
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                        multipart(UPLOAD_URL)
                                .file(file)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiResponse<Void> response =
                jacksonUtils.convertFromJson(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNotNull();

        return response.error();
    }

    private void assertApiError(
            ApiError actualError,
            ErrorCode expectedErrorCode
    ) {
        assertThat(actualError.code())
                .isEqualTo(expectedErrorCode.name());

        assertThat(actualError.message())
                .isEqualTo(expectedErrorCode.getDefaultMessage());
    }
}