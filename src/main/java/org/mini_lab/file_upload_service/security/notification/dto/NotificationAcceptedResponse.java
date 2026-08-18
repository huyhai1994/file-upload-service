package org.mini_lab.file_upload_service.security.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationAcceptedResponse(

        UUID eventId) {
}
