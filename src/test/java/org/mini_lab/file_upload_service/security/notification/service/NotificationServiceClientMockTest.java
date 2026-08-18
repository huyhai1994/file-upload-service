package org.mini_lab.file_upload_service.security.notification.service;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.notification.configuration.rest_client.NotificationClientProperties;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationAcceptedResponse;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationRequest;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(value = NotificationServiceClient.class)
@EnableConfigurationProperties(NotificationClientProperties.class)
@Import(NotificationServiceClientMockTest.MockClientConfiguration.class)
@ActiveProfiles("test")
class NotificationServiceClientMockTest {

    private static final String NOTIFICATION_URL =
            "http://notification-service:8080/api/v1/notifications";

    @Autowired
    private NotificationServiceClient notificationClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void send_whenResponseIsSuccessful_shouldDeserializeResponse() {
        UUID eventId = UUID.fromString(
                "b8a8ad36-7bec-4bed-92fc-f234263df128"
        );

        NotificationRequest request = new NotificationRequest(
                eventId,
                NotificationType.WELCOME_EMAIL,
                "minh@example.com",
                "minh"
        );

        mockServer.expect(requestTo(NOTIFICATION_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        content().json("""
                                {
                                  "eventId": "b8a8ad36-7bec-4bed-92fc-f234263df128",
                                  "notificationType": "WELCOME_EMAIL",
                                  "emailAddress": "minh@example.com",
                                  "username": "minh"
                                }
                                """)
                )
                .andRespond(
                        withSuccess(
                                """
                                        {
                                          "eventId": "b8a8ad36-7bec-4bed-92fc-f234263df128"
                                        }
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        NotificationAcceptedResponse response =
                notificationClient.send(request);

        assertThat(response).isNotNull();
        assertThat(response.eventId()).isEqualTo(eventId);

        mockServer.verify();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MockClientConfiguration {

        @Bean(name = "notificationRestClient")
        RestClient notificationRestClient(
                RestClient.Builder builder,
                NotificationClientProperties properties
        ) {
            return builder
                    .baseUrl(properties.baseUrl())
                    .defaultHeader(
                            HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .build();
        }
    }
}