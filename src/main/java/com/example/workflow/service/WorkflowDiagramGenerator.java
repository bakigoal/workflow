package com.example.workflow.service;

import com.example.workflow.entity.Transfer;
import com.example.workflow.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDiagramGenerator {

    private final TransferRepository transferRepository;

    public String generateMermaid(String processTypeCode) {
        var transfers = transferRepository.findByProcessTypeCode(processTypeCode);

        StringBuilder sb = new StringBuilder();
        sb.append("stateDiagram-v2\n");

        for (Transfer t : transfers) {
            String from = normalize(t.getStepTypeCodeSource());
            String to = normalize(t.getStepTypeCodeTarget());
            String signal = t.getSignalCode();

            if (t.getStepTypeCodeSource() == null) {
                sb.append("    [*] --> ")
                        .append(to)
                        .append(" : ")
                        .append(signal)
                        .append("\n");
            } else if (t.getStepTypeCodeTarget() == null) {
                sb.append("    ")
                        .append(from)
                        .append(" --> [*]")
                        .append(" : ")
                        .append(signal)
                        .append("\n");
            } else {
                sb.append("    ")
                        .append(from)
                        .append(" --> ")
                        .append(to)
                        .append(" : ")
                        .append(signal)
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private String normalize(String step) {
        return step == null ? "" : step;
    }
}
