package com.example.paastestbusiness.service;


import com.example.paastestbusiness.component.WorkflowValidator;
import com.example.paastestbusiness.dto.WorkflowrequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final WorkflowValidator workflowValidator;

    public void workflowExcute(WorkflowrequestDto request){


    }
}
