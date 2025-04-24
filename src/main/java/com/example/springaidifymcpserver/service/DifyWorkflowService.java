package com.example.springaidifymcpserver.service;

import com.example.springaidifymcpserver.model.dify.DifyWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dify工作流服务，提供工作流加载和处理功能
 */
@Service
@Slf4j
public class DifyWorkflowService {

    @Value("${dify.workflow.path:}")
    private String workflowPath;
    
    @Value("${dify.api.base-url:}")
    private String difyApiBaseUrl;
    
    @Value("${dify.api.api-key:}")
    private String difyApiKey;
    
    private final WebClient webClient;
    private final Map<String, DifyWorkflow> workflowCache = new ConcurrentHashMap<>();
    
    public DifyWorkflowService() {
        this.webClient = WebClient.builder()
                .baseUrl(difyApiBaseUrl)
                .build();
    }
    
    /**
     * 初始化并加载工作流
     */
    public void init() {
        if (workflowPath != null && !workflowPath.isEmpty()) {
            log.info("加载工作流文件: {}", workflowPath);
            try {
                File file = Paths.get(workflowPath).toFile();
                String workflowId = file.getName();
                DifyWorkflow workflow = loadWorkflowFromFile(file);
                workflowCache.put(workflowId, workflow);
                log.info("成功加载工作流: {}", workflow.getApp().getName());
            } catch (Exception e) {
                log.error("加载工作流失败: {}", e.getMessage(), e);
                throw new RuntimeException("无法加载工作流: " + e.getMessage());
            }
        } else {
            log.warn("未配置工作流路径，跳过加载");
        }
    }
    
    /**
     * 从文件加载工作流
     */
    public DifyWorkflow loadWorkflowFromFile(File file) {
        try {
            Yaml yaml = new Yaml();
            try (InputStream inputStream = new FileInputStream(file)) {
                return yaml.loadAs(inputStream, DifyWorkflow.class);
            }
        } catch (Exception e) {
            log.error("从文件加载工作流失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法从文件加载工作流: " + e.getMessage());
        }
    }
    
    /**
     * 添加工作流到缓存
     */
    public void addWorkflow(String workflowId, DifyWorkflow workflow) {
        workflowCache.put(workflowId, workflow);
    }
    
    /**
     * 获取工作流
     */
    public DifyWorkflow getWorkflow(String workflowId) {
        return workflowCache.get(workflowId);
    }
    
    /**
     * 获取所有工作流ID
     */
    public Map<String, String> getAllWorkflowIds() {
        Map<String, String> result = new HashMap<>();
        
        workflowCache.forEach((id, workflow) -> {
            result.put(id, workflow.getApp().getName());
        });
        
        return result;
    }
    
    /**
     * 分析工作流以提取MCP函数
     */
    public Map<String, Object> analyzeWorkflowForMcpFunction(String workflowId) {
        DifyWorkflow workflow = getWorkflow(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("找不到工作流: " + workflowId);
        }
        
        // 根据工作流分析生成函数定义
        Map<String, Object> function = new HashMap<>();
        function.put("name", workflow.getApp().getName().replaceAll("\\s+", "_").toLowerCase());
        function.put("description", workflow.getApp().getDescription());
        function.put("parameters", workflow.generateInputSchema());
        
        Map<String, Object> returnSchema = new HashMap<>();
        returnSchema.put("type", "object");
        returnSchema.put("properties", Map.of(
                "result", Map.of(
                        "type", "string",
                        "description", "处理结果"
                )
        ));
        function.put("returnSchema", returnSchema);
        
        return function;
    }
}