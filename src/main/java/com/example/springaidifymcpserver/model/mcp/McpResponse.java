package com.example.springaidifymcpserver.model.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * MCP响应模型，定义了服务器返回给客户端的响应格式
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {
    
    private Object result;
    
    @JsonProperty("content_type")
    private String contentType;
    
    private Map<String, Object> metadata;
    
    private Error error;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @Data
    @Builder
    public static class Error {
        private String code;
        private String message;
        private Map<String, Object> details;
    }
    
    /**
     * 创建成功响应
     */
    public static McpResponse success(Object result, String requestId, String sessionId) {
        return McpResponse.builder()
                .result(result)
                .contentType("application/json")
                .requestId(requestId)
                .sessionId(sessionId)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static McpResponse error(String code, String message, String requestId, String sessionId) {
        return McpResponse.builder()
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .build())
                .requestId(requestId)
                .sessionId(sessionId)
                .build();
    }
}