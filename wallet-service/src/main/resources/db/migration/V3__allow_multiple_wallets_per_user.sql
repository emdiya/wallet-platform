alter table wallets drop constraint if exists wallets_user_id_key;
alter table wallets drop constraint if exists uq_wallets_customer_id;

alter table wallets add constraint uq_wallets_user_currency unique (user_id, currency);
