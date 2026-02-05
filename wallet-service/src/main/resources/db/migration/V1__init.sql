create table wallets (
                         id bigserial primary key,
                         user_id bigint not null unique,
                         balance numeric(19,2) not null default 0,
                         version bigint not null default 0
);

create table wallet_holds (
                              id bigserial primary key,
                              hold_id varchar(80) not null unique,
                              user_id bigint not null,
                              amount numeric(19,2) not null,
                              status varchar(20) not null,
                              created_at timestamp not null default now()
);

create table wallet_operations (
                                   id bigserial primary key,
                                   operation_id varchar(80) not null unique,
                                   op_type varchar(30) not null,
                                   created_at timestamp not null default now()
);