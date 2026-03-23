alter table wallets add column customer_id varchar(32);
alter table wallets add column account_name varchar(120);
alter table wallets add column account_number varchar(20);
alter table wallets add column currency varchar(10) not null default 'USD';
alter table wallets add column created_at timestamp not null default now();

update wallets
set customer_id = 'CIDLEGACY' || lpad(user_id::text, 7, '0'),
    account_name = 'Wallet ' || user_id::text,
    account_number = '85501' || lpad(user_id::text, 10, '0')
where customer_id is null
   or account_name is null
   or account_number is null;

alter table wallets alter column customer_id set not null;
alter table wallets alter column account_name set not null;
alter table wallets alter column account_number set not null;

alter table wallets add constraint uq_wallets_customer_id unique (customer_id);
alter table wallets add constraint uq_wallets_account_number unique (account_number);

alter table wallet_holds add column wallet_id bigint;
alter table wallet_holds add column operation_id varchar(80);
alter table wallet_holds add column purpose varchar(255);
alter table wallet_holds add column processed_at timestamp;

update wallet_holds h
set wallet_id = w.id,
    operation_id = h.hold_id || '-legacy'
from wallets w
where w.user_id = h.user_id
  and (h.wallet_id is null or h.operation_id is null);

alter table wallet_holds alter column wallet_id set not null;
alter table wallet_holds alter column operation_id set not null;
alter table wallet_holds add constraint uq_wallet_holds_operation_id unique (operation_id);
