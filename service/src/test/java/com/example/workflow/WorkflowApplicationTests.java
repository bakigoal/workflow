package com.example.workflow;

import com.example.workflow.config.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestcontainersConfiguration.class)
class WorkflowApplicationTests {

    @Test
    void contextLoads() {
    }

}
