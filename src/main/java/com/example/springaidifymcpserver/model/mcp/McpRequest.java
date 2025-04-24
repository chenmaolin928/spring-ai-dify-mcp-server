package com.example.springaidifymcpserver.model.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * MCP请求模型，定义了客户端发送给服务器的请求格式
 */
@Data
public class McpRequest {
    
    private String name;
    
    private Map<String, Object> arguments;
    
    // 可选属性，用于追踪请求
    @JsonProperty("request_id")
    private String requestId;
    
    // 客户端会话ID
    @JsonProperty("session_id")
    private String sessionId;
    
    // 用户上下文信息
    private Map<String, Object> context;
}