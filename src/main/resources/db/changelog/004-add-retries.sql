alter table step_instance
    add column retry_count int default 0,
    add column next_retry_at timestamptz,
    add column retry_claimed_at timestamptz;

create index idx_retry_ready
    on step_instance (next_retry_at)
    where end_time is null;

create index idx_retry_claimed
    on step_instance (retry_claimed_at)
    where end_time is null;


-- STEP_B -> END
insert into transfer (
    code,
    process_type_code,
    step_type_code_source,
    step_type_code_target,
    signal_code
)
values (
    'PROC1_STEP_B_RETRY',
    'PROC1',
    'STEP_B',
    'STEP_B',
    'RETRY'
);