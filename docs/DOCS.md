1️⃣ Общая архитектура Workflow Engine

```mermaid
classDiagram
    class WorkflowEngine
    class StepHandlerRegistry
    class StepHandler {
        <<interface>>
    }

    class ProcessInstance
    class StepInstance
    class Transfer

    WorkflowEngine --> StepHandlerRegistry
    StepHandlerRegistry --> StepHandler

    WorkflowEngine --> ProcessInstance
    WorkflowEngine --> StepInstance
    WorkflowEngine --> Transfer
```

WorkflowEngine — это orchestration-слой.
Он не знает деталей шагов, только управляет состоянием и переходами.
Логика шагов инкапсулирована в StepHandler’ах.

---

2️⃣ FSM / Execution Flow (ключевая диаграмма)

```mermaid
stateDiagram-v2
    [*] --> START

    START --> STEP_A: START
    STEP_A --> STEP_B: OK
    STEP_B --> STEP_ERROR: ERROR
    STEP_B --> END: SUCCESS

    STEP_A --> STEP_A: retry / backoff
    STEP_B --> STEP_B: retry / backoff

    STEP_ERROR --> END
    END --> [*]
```

Это конечный автомат.
Переходы определяются сигналами, а retry — это просто возврат в тот же шаг с задержкой.

---

3️⃣ Sequence diagram — pause / resume (очень сильная)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant WorkflowEngine
    participant StepHandler
    participant DB

    Client->>Controller: POST /process/start
    Controller->>WorkflowEngine: execute(process, START)

    WorkflowEngine->>DB: find active step
    WorkflowEngine->>StepHandler: handle(step)

    StepHandler-->>WorkflowEngine: null (pause)

    WorkflowEngine->>DB: commit state
    Controller-->>Client: 202 Accepted + processId

    Note over Client,Controller: Resume later

    Client->>Controller: POST /process/{id}/resume
    Controller->>WorkflowEngine: execute(process, SIGNAL)

    WorkflowEngine->>StepHandler: handle(step)
    StepHandler-->>WorkflowEngine: NEXT_SIGNAL

    WorkflowEngine->>DB: persist step + transition
```

Pause — это осознанное завершение транзакции без продолжения процесса.
Resume — повторный запуск engine с новым сигналом.

---

4️⃣ Optimistic Locking (конкурентный запуск)

```mermaid
sequenceDiagram
    participant Engine_A
    participant Engine_B
    participant DB

    Engine_A->>DB: load Process (v=1)
    Engine_B->>DB: load Process (v=1)

    Engine_A->>DB: update Process (v=2)
    Engine_B->>DB: update Process (v=2)

    DB-->>Engine_B: OptimisticLockException
    Engine_B-->>Engine_B: exit gracefully
```

Конкуренция разрешается optimistic locking’ом.
Один из потоков безопасно проигрывает — процесс остаётся консистентным.

---

5️⃣ Retry + Backoff (scheduler)

```mermaid
sequenceDiagram
    participant Scheduler
    participant WorkflowEngine
    participant DB

    Scheduler->>DB: find steps where nextRetryAt <= now
    Scheduler->>WorkflowEngine: execute(process, RETRY)

    WorkflowEngine->>DB: update retryCount
    WorkflowEngine->>DB: set nextRetryAt
```

Retry управляется данными, а не кодом.
Engine просто исполняет то, что готово к выполнению.