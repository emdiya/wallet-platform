alter table users add column customer_id varchar(32);
alter table users add column account_name varchar(120);
alter table users add column account_number varchar(20);

update users
set customer_id = 'CID' || to_char(created_at, 'YYYYMM') || lpad(id::text, 8, '0'),
    account_name = full_name,
    account_number = '85501' || lpad(id::text, 10, '0')
where customer_id is null
   or account_name is null
   or account_number is null;

alter table users alter column customer_id set not null;
alter table users alter column account_name set not null;
alter table users alter column account_number set not null;

alter table users add constraint uq_users_customer_id unique (customer_id);
alter table users add constraint uq_users_account_number unique (account_number);
