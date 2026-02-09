package com.opticstore.Analytics.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AnalyticsResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String period;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public static AnalyticsResponseDTO success(Object data, String period) {
        AnalyticsResponseDTO response = new AnalyticsResponseDTO();
        response.setSuccess(true);
        response.setData(data);
        response.setPeriod(period);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static AnalyticsResponseDTO error(String message) {
        AnalyticsResponseDTO response = new AnalyticsResponseDTO();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}