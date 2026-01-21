-- START -> S_A
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'TEST_START_S_A',
    'TEST',
    null,
    'S_A',
    'START'
);

-- S_A -> S_B
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'TEST_S_A_S_B',
    'TEST',
    'S_A',
    'S_B',
    'NEXT'
);

-- S_B -> S_C
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'TEST_S_B_S_C',
    'TEST',
    'S_B',
    'S_C',
    'NEXT'
);

-- S_C -> S_C RETRY
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'TEST_S_C_S_C',
    'TEST',
    'S_C',
    'S_C',
    'RETRY'
);

-- S_C -> END
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'TEST_S_C_END',
    'TEST',
    'S_C',
    null,
    'FINISH'
);
