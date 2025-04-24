package com.example.springaidifymcpserver.model.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * MCP函数模型，定义了函数的结构和参数
 */
@Data
@Builder
public class McpFunction {
    private String name;
    private String description;
    private Map<String, Object> parameters;
    
    @JsonProperty("return_schema")
    private Map<String, Object> returnSchema;
    
    private String category;
    
    // 添加Dify相关属性
    @JsonProperty("dify_workflow_id")
    private String difyWorkflowId;
    
    @JsonProperty("dify_node_path")
    private String difyNodePath;
    
    // 添加Schema相关属性
    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;
}