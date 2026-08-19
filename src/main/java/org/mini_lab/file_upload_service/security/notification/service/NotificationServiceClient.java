package org.mini_lab.file_upload_service.security.notification.service;

import org.mini_lab.file_upload_service.security.notification.configuration.rest_client.NotificationClientProperties;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationAcceptedResponse;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.mini_lab.file_upload_service.security.notification.exception.NotificationServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationServiceClient {
    private final RestClient notificationRestClient;
    private final NotificationClientProperties properties;

    public NotificationServiceClient(@Qualifier("notificationRestClient") RestClient notificationRestClient, NotificationClientProperties properties) {
        this.notificationRestClient = notificationRestClient;
        this.properties = properties;
    }

    public NotificationAcceptedResponse send(
            UserRegisteredEvent request
    ) {
        return notificationRestClient.post()
                .uri(properties.baseUri())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (httpRequest, response) -> {
                            throw new NotificationServiceException(
                                    response.getStatusCode()
                            );
                        }
                )
                .body(NotificationAcceptedResponse.class);
    }

}
