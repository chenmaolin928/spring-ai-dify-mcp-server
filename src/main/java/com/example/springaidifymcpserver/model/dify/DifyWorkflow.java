package com.example.springaidifymcpserver.model.dify;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Dify工作流模型类，用于解析和表示Dify工作流YAML文件
 */
@Data
public class DifyWorkflow {
    private App app;
    private String kind;
    private String version;
    private Workflow workflow;
    
    @Data
    public static class App {
        private String description;
        private String icon;
        private String iconBackground;
        private String mode;
        private String name;
        private boolean useIconAsAnswerIcon;
    }
    
    @Data
    public static class Workflow {
        private List<Object> conversationVariables;
        private List<Object> environmentVariables;
        private Features features;
        private Graph graph;
    }
    
    @Data
    public static class Features {
        private FileUpload fileUpload;
        private String openingStatement;
        private RetrieverResource retrieverResource;
        private Map<String, Object> sensitiveWordAvoidance;
        private Map<String, Object> speechToText;
        private List<Object> suggestedQuestions;
        private Map<String, Object> suggestedQuestionsAfterAnswer;
        private Map<String, Object> textToSpeech;
        
        @Data
        public static class FileUpload {
            private List<String> allowedFileExtensions;
            private List<String> allowedFileTypes;
            private List<String> allowedFileUploadMethods;
            private boolean enabled;
            private Map<String, Object> fileUploadConfig;
            private Map<String, Object> image;
            private int numberLimits;
        }
        
        @Data
        public static class RetrieverResource {
            private boolean enabled;
        }
    }
    
    @Data
    public static class Graph {
        private List<Edge> edges;
        private List<Node> nodes;
        private Viewport viewport;
        
        @Data
        public static class Edge {
            private EdgeData data;
            private String id;
            private String source;
            private String sourceHandle;
            private String target;
            private String targetHandle;
            private String type;
            
            @Data
            public static class EdgeData {
                private String sourceType;
                private String targetType;
            }
        }
        
        @Data
        public static class Node {
            private NodeData data;
            private boolean dragging;
            private int height;
            private String id;
            private Position position;
            private Position positionAbsolute;
            private boolean selected;
            private String sourcePosition;
            private String targetPosition;
            private String type;
            private int width;
            
            @Data
            public static class NodeData {
                private String answer;
                private List<NodeClass> classes;
                private String desc;
                private Context context;
                private List<String> datasetIds;
                private Map<String, Object> memory;
                private Map<String, Object> model;
                private List<Map<String, Object>> promptTemplate;
                private List<String> queryVariableSelector;
                private String retrievalMode;
                private boolean selected;
                private Map<String, Object> singleRetrievalConfig;
                private String title;
                private List<String> topics;
                private String type;
                private List<Variable> variables;
                private Map<String, Object> vision;
                
                @Data
                public static class NodeClass {
                    private String id;
                    private String name;
                }
                
                @Data
                public static class Context {
                    private boolean enabled;
                    private List<String> variableSelector;
                }
                
                @Data
                public static class Variable {
                    private List<String> valueSelector;
                    private String variable;
                }
            }
            
            @Data
            public static class Position {
                private double x;
                private double y;
            }
        }
        
        @Data
        public static class Viewport {
            private int x;
            private double y;
            private double zoom;
        }
    }
    
    /**
     * 根据节点ID获取节点
     */
    public Graph.Node getNodeById(String nodeId) {
        return workflow.getGraph().getNodes().stream()
                .filter(node -> nodeId.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取工作流的起始节点
     */
    public Graph.Node getStartNode() {
        return workflow.getGraph().getNodes().stream()
                .filter(node -> "start".equals(node.getData().getType()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取从指定节点出发的边
     */
    public List<Graph.Edge> getEdgesFromNode(String nodeId) {
        return workflow.getGraph().getEdges().stream()
                .filter(edge -> nodeId.equals(edge.getSource()))
                .toList();
    }
    
    /**
     * 根据工作流分析生成函数输入模式
     */
    public Map<String, Object> generateInputSchema() {
        // 这里只是一个简单实现，真实情况下需要根据工作流结构和节点类型生成
        Map<String, Object> properties = Map.of(
                "query", Map.of(
                        "type", "string",
                        "description", "用户查询内容"
                )
        );
        
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("query")
        );
    }
}