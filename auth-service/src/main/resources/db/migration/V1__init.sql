create table users (
                       id bigserial primary key,
                       full_name varchar(120) not null,
                       phone varchar(30) not null unique,
                       password_hash varchar(255) not null,
                       created_at timestamp not null default now()
);