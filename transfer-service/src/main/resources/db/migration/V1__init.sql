create table transfers (
                           id bigserial primary key,
                           request_id varchar(80) not null unique,
                           from_user_id bigint not null,
                           to_user_id bigint not null,
                           amount numeric(19,2) not null,
                           status varchar(20) not null,
                           hold_id varchar(80),
                           error_code varchar(50),
                           error_message varchar(255),
                           created_at timestamp not null default now()
);