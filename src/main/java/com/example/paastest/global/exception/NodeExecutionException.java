package com.example.paastest.global.exception;

public class NodeExecutionException extends RuntimeException {

    // 이 에러 객체가 태어날 때, '어느 노드에서 죽었는지' 이름을 뱃지처럼 달고 태어나게 할 겁니다.
    private final String nodeName;

    // 생성자: 에러를 던질 때 노드 이름, 메시지, 그리고 진짜 원인(cause)을 받습니다.
    public NodeExecutionException(String nodeName, String message, Throwable cause) {
        super(message, cause); // 부모 클래스(기본 에러 처리기)에게 기본 정보 전달
        this.nodeName = nodeName; // 우리만의 특별한 정보(노드 이름) 저장
    }

    // 나중에 GlobalExceptionHandler에서 이 노드 이름을 꺼내볼 수 있게 해주는 게터(Getter)
    public String getNodeName() {
        return nodeName;
    }
}