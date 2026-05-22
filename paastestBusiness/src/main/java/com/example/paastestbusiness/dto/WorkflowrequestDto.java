package com.example.paastestbusiness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;
@Getter
public class WorkflowrequestDto {
    @NotBlank(message = "워크플로우 이름은 필수입니다.")
    private String workflowName;
    @NotNull(message = "워크플로우 노드는 필수입니다.")
    private List<NodeDto> nodes;
    @NotNull(message = "워크플로우 연결은 필수입니다.")
    private List<EdgeDto> edges;
    private providerSetting provider;

    @Getter
    public static class NodeDto{
        private String id;
        private String type;
        private String name;

    }
    @Getter
    public static class EdgeDto{
        private String source;
        private String target;

    }
    @Getter
    public static class providerSetting{
        private String targetUrl;
        private String apiToken;
    }
}
