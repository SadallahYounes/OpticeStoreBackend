package com.opticstore.notifications.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError((e) -> emitters.remove(clientId));

        // Send initial connection success
        sendEvent(emitter, "connected", Map.of("message", "SSE connection established"));

        return emitter;
    }

    public void sendToAll(String eventName, Object data) {
        emitters.forEach((clientId, emitter) -> {
            try {
                sendEvent(emitter, eventName, data);
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(clientId);
            }
        });
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(eventName)
                    .data(data, MediaType.APPLICATION_JSON);
            emitter.send(event);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public void removeEmitter(String clientId) {
        emitters.remove(clientId);
    }
}