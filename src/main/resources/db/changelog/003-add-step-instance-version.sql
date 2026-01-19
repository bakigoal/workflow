alter table step_instance
    add column version bigint not null default 0;