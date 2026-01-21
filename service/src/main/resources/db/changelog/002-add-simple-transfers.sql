-- START -> STEP_A
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_START_STEP_A',
    'PROC1',
    null,
    'STEP_A',
    'START'
);

-- STEP_A -> STEP_B
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_STEP_A_STEP_B',
    'PROC1',
    'STEP_A',
    'STEP_B',
    'NEXT'
);

-- STEP_B -> END
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_STEP_B_END',
    'PROC1',
    'STEP_B',
    null,
    'FINISH'
);

-- STEP_B -> STEP_ERROR
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_STEP_B_STEP_ERROR',
    'PROC1',
    'STEP_B',
    'STEP_ERROR',
    'ERROR'
);

-- STEP_ERROR -> END
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_STEP_ERROR_END',
    'PROC1',
    'STEP_ERROR',
    null,
    'FINISH'
);