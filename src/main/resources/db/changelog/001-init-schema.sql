create table process_instance (
    id uuid primary key,
    process_type_code varchar(100) not null,
    start_time timestamptz,
    end_time timestamptz
);

create table step_instance (
    id uuid primary key,
    process_instance_id uuid not null,
    step_type_code varchar(100),
    transfer_code varchar(100),
    start_time timestamptz,
    end_time timestamptz,
    constraint fk_step_process
        foreign key (process_instance_id)
        references process_instance(id)
        on delete cascade
);

create unique index ux_active_step
    on step_instance(process_instance_id)
    where end_time is null;

create table transfer (
    code varchar(100) primary key,
    process_type_code varchar(100) not null,
    step_type_code_source varchar(100),
    step_type_code_target varchar(100),
    signal_code varchar(100) not null
);