package org.mini_lab.file_upload_service.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileUploadControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void uploadFile_whenFileIsEmpty_thenReturnBadRequest() throws Exception {
        MockMultipartFile mockMultipartFile =
                MockObjectBuilder.getEmptyMultipartFile();

        mockMvc.perform(multipart("/api/v1/files")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("File Empty"));
    }

    @Test
    void uploadFile_whenFileNameInvalid_thenReturnBadRequest() throws Exception {
        MockMultipartFile mockMultipartFile =
                MockObjectBuilder.getEmptyFilenameMultipartFile();

        mockMvc.perform(multipart("/api/v1/files")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("File name not valid"));
    }

    @Test
    void uploadFile_whenFileContentAndExtensionMismatch_thenReturnBadRequest() throws Exception {
        MockMultipartFile mockMultipartFile =
                MockObjectBuilder.getMismatchMimeMultipartFile();

        mockMvc.perform(multipart("/api/v1/files")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Mime type  not valid"));
    }

    @Test
    void uploadFile_whenFileExtensionInvalid_ThenReturnBadRequest() throws Exception {
        MockMultipartFile mockMultipartFile =
                MockObjectBuilder.getNonValidExtensionMultipartFile();

        mockMvc.perform(multipart("/api/v1/files")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("File extension not valid"));
    }

    @Test
    void uploadFile_whenFileIsValid_thenReturnOk() throws Exception {
        MockMultipartFile mockMultipartFile =
                MockObjectBuilder.getTextContentTypeMultipartFile();

        MvcResult result = mockMvc.perform(multipart("/api/v1/files")
                        .file(mockMultipartFile))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String responseBody = result.getResponse().getContentAsString();

        ApiResponse<FileMetadataResponseDTO> response = objectMapper.readValue(responseBody, new TypeReference<>() {
        });
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();
        assertThat(response.apiError()).isNull();

        assertThat(response.data().fileId()).isNotNull();
        assertThat(response.data().fileName()).isNotNull();
        assertThat(response.data().contentType()).isNotNull();
        assertThat(response.data().size()).isNotNull();
        assertThat(response.data().checksum()).isNotNull();
        assertThat(response.data().state()).isNotNull();
        assertThat(response.data().createdAt()).isNotNull();
        assertThat(response.data().state()).isEqualTo(FileState.COMPLETED);
    }
}