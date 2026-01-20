1️⃣ Общая архитектура Workflow Engine

```plantuml
package "Workflow Engine" {

class WorkflowEngine
class StepHandlerRegistry
interface StepHandler

class ProcessInstance
class StepInstance
class Transfer

WorkflowEngine --> StepHandlerRegistry
StepHandlerRegistry --> StepHandler

WorkflowEngine --> ProcessInstance
WorkflowEngine --> StepInstance
WorkflowEngine --> Transfer
}

package "Persistence (DB)" {
ProcessInstance
StepInstance
Transfer
}
```

WorkflowEngine — это orchestration-слой.
Он не знает деталей шагов, только управляет состоянием и переходами.
Логика шагов инкапсулирована в StepHandler’ах.

---

2️⃣ FSM / Execution Flow (ключевая диаграмма)

```plantuml
@startuml
[*] --> START

START --> STEP_A : Signal.START
STEP_A --> STEP_B : Signal.OK
STEP_B --> STEP_ERROR : Signal.ERROR
STEP_B --> END : Signal.SUCCESS

STEP_A --> STEP_A : retry / backoff
STEP_B --> STEP_B : retry / backoff

STEP_ERROR --> END

END --> [*]
@enduml
```

Это конечный автомат.
Переходы определяются сигналами, а retry — это просто возврат в тот же шаг с задержкой.

---

3️⃣ Sequence diagram — pause / resume (очень сильная)

```plantuml
@startuml
actor Client
participant Controller
participant WorkflowEngine
participant StepHandler
database DB

Client -> Controller : POST /process/start
Controller -> WorkflowEngine : execute(process, START)

WorkflowEngine -> DB : find active step
WorkflowEngine -> StepHandler : handle(step)

StepHandler --> WorkflowEngine : null (pause)

WorkflowEngine -> DB : commit state
Controller --> Client : 202 Accepted + processId

== Resume ==

Client -> Controller : POST /process/{id}/resume
Controller -> WorkflowEngine : execute(process, SIGNAL)

WorkflowEngine -> StepHandler : handle(step)
StepHandler --> WorkflowEngine : NEXT_SIGNAL

WorkflowEngine -> DB : persist step + transition
@enduml
```

Pause — это осознанное завершение транзакции без продолжения процесса.
Resume — повторный запуск engine с новым сигналом.

---

4️⃣ Optimistic Locking (конкурентный запуск)

```plantuml
@startuml
participant Engine_A
participant Engine_B
database DB

Engine_A -> DB : load ProcessInstance (v=1)
Engine_B -> DB : load ProcessInstance (v=1)

Engine_A -> DB : update (v=2)
Engine_B -> DB : update (v=2)

DB --> Engine_B : OptimisticLockException
Engine_B -> Engine_B : exit gracefully
@enduml
```

Конкуренция разрешается optimistic locking’ом.
Один из потоков безопасно проигрывает — процесс остаётся консистентным.

---

5️⃣ Retry + Backoff (scheduler)

```plantuml
@startuml
participant Scheduler
participant WorkflowEngine
database DB

Scheduler -> DB : find steps where next_retry_at <= now
Scheduler -> WorkflowEngine : execute(process, RETRY_SIGNAL)

WorkflowEngine -> DB : update retry_count
WorkflowEngine -> DB : set next_retry_at
@enduml
```

Retry управляется данными, а не кодом.
Engine просто исполняет то, что готово к выполнению.