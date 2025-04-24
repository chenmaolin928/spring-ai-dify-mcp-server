package com.example.springaidifymcpserver.service;

import com.example.springaidifymcpserver.model.dify.DifyWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流执行器，负责执行Dify工作流
 */
@Service
@Slf4j
public class WorkflowExecutor {

    private final ChatClient chatClient;
    
    @Autowired
    public WorkflowExecutor(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * 执行工作流
     */
    public Map<String, Object> executeWorkflow(DifyWorkflow workflow, Map<String, Object> inputs) {
        log.info("开始执行工作流: {}", workflow.getApp().getName());
        
        // 获取用户查询
        String query = (String) inputs.getOrDefault("query", "");
        if (query.isEmpty()) {
            throw new IllegalArgumentException("输入参数'query'不能为空");
        }
        
        // 准备上下文
        Map<String, Object> context = new HashMap<>();
        context.put("sys.query", query);
        
        // 获取起始节点
        DifyWorkflow.Graph.Node startNode = workflow.getStartNode();
        if (startNode == null) {
            throw new IllegalStateException("无法找到工作流起始节点");
        }
        
        // 执行工作流
        String result = processNode(workflow, startNode.getId(), context);
        
        // 返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        return response;
    }
    
    /**
     * 处理工作流节点
     */
    private String processNode(DifyWorkflow workflow, String nodeId, Map<String, Object> context) {
        // 获取当前节点
        DifyWorkflow.Graph.Node node = workflow.getNodeById(nodeId);
        if (node == null) {
            throw new IllegalStateException("找不到节点: " + nodeId);
        }
        
        log.debug("处理节点: {} ({}) - ID: {}", node.getData().getTitle(), node.getData().getType(), node.getId());
        
        // 根据节点类型处理
        String nodeType = node.getData().getType();
        String result;
        
        switch (nodeType) {
            case "start":
                // 起始节点，直接处理下一个节点
                result = processNextNode(workflow, node, context);
                break;
                
            case "question-classifier":
                // 问题分类节点
                result = processQuestionClassifier(workflow, node, context);
                break;
                
            case "knowledge-retrieval":
                // 知识检索节点
                result = processKnowledgeRetrieval(workflow, node, context);
                break;
                
            case "llm":
                // LLM处理节点
                result = processLlmNode(workflow, node, context);
                break;
                
            case "answer":
                // 回答节点
                result = processAnswerNode(workflow, node, context);
                break;
                
            default:
                log.warn("未支持的节点类型: {}", nodeType);
                result = "不支持的节点类型: " + nodeType;
        }
        
        return result;
    }
    
    /**
     * 处理问题分类节点
     */
    private String processQuestionClassifier(DifyWorkflow workflow, DifyWorkflow.Graph.Node node, Map<String, Object> context) {
        String query = (String) context.get("sys.query");
        log.debug("执行问题分类: {}", query);
        
        List<DifyWorkflow.Graph.Node.NodeData.NodeClass> classes = node.getData().getClasses();
        if (classes == null || classes.isEmpty()) {
            log.warn("问题分类节点没有定义类别");
            return processNextNode(workflow, node, context);
        }
        
        // 构建分类提示
        StringBuilder prompt = new StringBuilder();
        prompt.append("对以下问题进行分类，只返回最匹配类别的ID：\n\n");
        prompt.append("问题: ").append(query).append("\n\n");
        prompt.append("类别:\n");
        
        classes.forEach(cls -> {
            prompt.append("- ID: ").append(cls.getId())
                  .append(", 名称: ").append(cls.getName()).append("\n");
        });
        
        // 使用ChatClient分类
        Prompt classifierPrompt = new Prompt(new UserMessage(prompt.toString()));
        ChatResponse response = chatClient.call(classifierPrompt);
        String classId = response.getResult().getOutput().getContent().trim();
        
        log.debug("分类结果ID: {}", classId);
        
        // 查找匹配的边
        List<DifyWorkflow.Graph.Edge> matchingEdges = workflow.getEdgesFromNode(node.getId()).stream()
                .filter(edge -> classId.equals(edge.getSourceHandle()))
                .toList();
        
        if (matchingEdges.isEmpty()) {
            log.warn("找不到与分类ID匹配的边: {}", classId);
            // 尝试找到默认边
            List<DifyWorkflow.Graph.Edge> defaultEdges = workflow.getEdgesFromNode(node.getId());
            if (!defaultEdges.isEmpty()) {
                return processNode(workflow, defaultEdges.get(0).getTarget(), context);
            } else {
                return "无法处理查询，找不到匹配的分类路径";
            }
        }
        
        // 处理匹配的第一条边
        return processNode(workflow, matchingEdges.get(0).getTarget(), context);
    }
    
    /**
     * 处理知识检索节点
     */
    private String processKnowledgeRetrieval(DifyWorkflow workflow, DifyWorkflow.Graph.Node node, Map<String, Object> context) {
        String query = (String) context.get("sys.query");
        log.debug("执行知识检索: {}", query);
        
        // 这里应该连接实际的知识库进行检索
        // 由于是示例实现，这里只是模拟检索结果
        String retrievedContent = "这是一个模拟的知识检索结果，包含与用户查询\"" + query + "\"相关的信息。";
        
        // 将检索结果添加到上下文
        context.put(node.getId() + ".result", retrievedContent);
        
        // 处理下一个节点
        return processNextNode(workflow, node, context);
    }
    
    /**
     * 处理LLM节点
     */
    private String processLlmNode(DifyWorkflow workflow, DifyWorkflow.Graph.Node node, Map<String, Object> context) {
        log.debug("执行LLM节点");
        
        // 获取上下文内容
        String contextContent = "";
        if (node.getData().getContext() != null && node.getData().getContext().isEnabled()) {
            List<String> variableSelector = node.getData().getContext().getVariableSelector();
            if (variableSelector != null && variableSelector.size() >= 2) {
                String contextNodeId = variableSelector.get(0);
                String contextVarName = variableSelector.get(1);
                String contextKey = contextNodeId + "." + contextVarName;
                contextContent = (String) context.getOrDefault(contextKey, "");
            }
        }
        
        // 获取系统提示
        String systemPrompt = "";
        if (node.getData().getPromptTemplate() != null && !node.getData().getPromptTemplate().isEmpty()) {
            for (Map<String, Object> template : node.getData().getPromptTemplate()) {
                if ("system".equals(template.get("role"))) {
                    systemPrompt = (String) template.get("text");
                    break;
                }
            }
        }
        
        // 替换模板变量
        if (systemPrompt.contains("{{#context#}}")) {
            systemPrompt = systemPrompt.replace("{{#context#}}", contextContent);
        }
        
        // 用户查询
        String userQuery = (String) context.get("sys.query");
        
        // 使用ChatClient调用LLM
        Prompt prompt = new Prompt(
                Arrays.asList(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userQuery)
                ));
        
        ChatResponse response = chatClient.call(prompt);
        String result = response.getResult().getOutput().getContent();
        
        // 保存结果到上下文
        context.put(node.getId() + ".text", result);
        
        // 处理下一个节点
        return processNextNode(workflow, node, context);
    }
    
    /**
     * 处理回答节点
     */
    private String processAnswerNode(DifyWorkflow workflow, DifyWorkflow.Graph.Node node, Map<String, Object> context) {
        log.debug("执行回答节点");
        
        // 获取回答内容
        String answer = node.getData().getAnswer();
        
        // 处理变量引用
        if (answer != null && answer.startsWith("{{#") && answer.endsWith("#}}")) {
            String varRef = answer.substring(3, answer.length() - 3);
            answer = (String) context.getOrDefault(varRef, "无法获取回答内容");
        }
        
        return answer;
    }
    
    /**
     * 处理下一个节点
     */
    private String processNextNode(DifyWorkflow workflow, DifyWorkflow.Graph.Node node, Map<String, Object> context) {
        List<DifyWorkflow.Graph.Edge> edges = workflow.getEdgesFromNode(node.getId());
        
        if (edges.isEmpty()) {
            log.debug("节点没有连接的下游节点，工作流执行结束");
            return "工作流执行完成";
        }
        
        // 处理第一条边指向的节点
        return processNode(workflow, edges.get(0).getTarget(), context);
    }
}