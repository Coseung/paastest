package com.example.paastestbusiness.component;

import com.example.paastestbusiness.dto.WorkflowrequestDto;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WorkflowValidator {
    public void validate(WorkflowrequestDto definition) {
        if (definition == null) {
            throw new IllegalArgumentException("워크플로우 요청 데이터가 비어있습니다.");
        }

        validateNodesAndRequiredFields(definition);
        validateEdgesAndNoCycles(definition);
    }

    private void validateNodesAndRequiredFields(WorkflowrequestDto definition) {
        List<WorkflowrequestDto.NodeDto> nodes = definition.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("워크플로우에 최소 1개 이상의 노드가 존재해야 합니다.");
        }

        Set<String> duplicateCheckSet = new HashSet<>();

        for (WorkflowrequestDto.NodeDto node : nodes) {
            // 노드 ID 중복 검증
            if (!duplicateCheckSet.add(node.getId())) {
                throw new IllegalArgumentException("중복된 노드 ID가 존재합니다. 중복된 ID: " + node.getId());
            }

            // ERP_PROVIDER 타입인 경우 apiToken 필수 검증
            if ("ERP_PROVIDER".equals(node.getType())) {
                if (definition.getProvider() == null ||
                        definition.getProvider().getApiToken() == null ||
                        definition.getProvider().getApiToken().isBlank()) {
                    throw new IllegalArgumentException("ERP 연동을 위해 apiToken은 필수입니다.");
                }
            }
        }
    }

    private void validateEdgesAndNoCycles(WorkflowrequestDto definition) {
        List<WorkflowrequestDto.NodeDto> nodes = definition.getNodes();
        List<WorkflowrequestDto.EdgeDto> edges = definition.getEdges();

        if (edges == null || edges.isEmpty()) {
            return;
        }

        Set<String> allNodeIds = new HashSet<>();
        for (WorkflowrequestDto.NodeDto node : nodes) {
            allNodeIds.add(node.getId());
        }

        // 인접 리스트(그래프) 구조 생성
        Map<String, List<String>> graph = new HashMap<>();
        for (String nodeId : allNodeIds) {
            graph.put(nodeId, new ArrayList<>());
        }

        for (WorkflowrequestDto.EdgeDto edge : edges) {
            String source = edge.getSource();
            String target = edge.getTarget();

            if (target == null || target.isBlank() || !allNodeIds.contains(target)) {
                throw new IllegalArgumentException("존재하지 않거나 비어있는 타겟 노드로의 엣지가 발견되었습니다. Target: " + target);
            }
            if (source == null || source.isBlank() || !allNodeIds.contains(source)) {
                throw new IllegalArgumentException("존재하지 않거나 비어있는 출발지 노드로부터의 엣지가 발견되었습니다. Source: " + source);
            }
            if (source.equals(target)) {
                throw new IllegalArgumentException("노드가 자기 자신을 타겟팅할 수 없습니다. 노드 ID: " + source);
            }

            graph.get(source).add(target);
        }

        Set<String> visiting = new HashSet<>();
        Set<String> completed = new HashSet<>();

        for (String nodeId : graph.keySet()) {
            if (!completed.contains(nodeId)) {
                dfsCheckCycle(nodeId, graph, visiting, completed);
            }
        }
    }

    private void dfsCheckCycle(String currentNode, Map<String, List<String>> graph,
                               Set<String> visiting, Set<String> completed) {

        // 현재 탐색 경로 내에서 기방문 노드를 만난 경우 (순환 참조 발생)
        if (visiting.contains(currentNode)) {
            throw new IllegalArgumentException("워크플로우 내에 무한 루프를 유발하는 순환 참조 경로가 탐지되었습니다. 노드 ID: " + currentNode);
        }

        // 완전히 검증이 완료된 노드인 경우 탐색 스킵
        if (completed.contains(currentNode)) {
            return;
        }
        visiting.add(currentNode);

        List<String> nextNodes = graph.getOrDefault(currentNode, Collections.emptyList());
        for (String nextNode : nextNodes) {
            dfsCheckCycle(nextNode, graph, visiting, completed);
        }

        visiting.remove(currentNode);
        completed.add(currentNode);
    }
}
