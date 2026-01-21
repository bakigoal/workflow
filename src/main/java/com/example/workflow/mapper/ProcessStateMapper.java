package com.example.workflow.mapper;

import com.example.workflow.core.models.ProcessState;
import com.example.workflow.entity.ProcessInstance;
import org.springframework.stereotype.Component;

@Component
public class ProcessStateMapper {

    public ProcessState toState(ProcessInstance p) {
        var state = new ProcessState();
        state.setId(p.getId());
        state.setProcessTypeCode(p.getProcessTypeCode());
        state.setStartTime(p.getStartTime());
        state.setEndTime(p.getEndTime());
        return state;
    }
}
