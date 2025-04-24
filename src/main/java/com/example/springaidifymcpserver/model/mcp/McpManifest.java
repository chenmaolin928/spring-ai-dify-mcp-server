package com.example.springaidifymcpserver.model.mcp;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP服务器清单模型，提供关于服务器功能的元数据
 */
@Data
@Builder
public class McpManifest {
    private ServerInfo server;
    private List<McpFunction> functions;
    
    @Data
    @Builder
    public static class ServerInfo {
        private String id;
        private String name;
        private String description;
        private String version;
        private String vendor;
        private List<String> tags;
        private String homepage;
        private Auth auth;
        
        @Data
        @Builder
        public static class Auth {
            private String type; // "none", "bearer", "oauth", etc.
            private Map<String, Object> config;
        }
    }
}