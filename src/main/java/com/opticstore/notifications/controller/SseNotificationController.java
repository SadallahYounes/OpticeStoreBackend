package com.opticstore.notifications.controller;

import com.opticstore.notifications.service.SseEmitterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/sse")
@CrossOrigin
public class SseNotificationController {

    private final SseEmitterService sseEmitterService;

    public SseNotificationController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(HttpServletRequest request) {
        // Generate a unique client ID ( use session ID or authentication token)
        String clientId = UUID.randomUUID().toString();

        // in authentication cases, get the user ID:
        // String clientId = SecurityContextHolder.getContext().getAuthentication().getName();

        return sseEmitterService.createEmitter(clientId);
    }
}