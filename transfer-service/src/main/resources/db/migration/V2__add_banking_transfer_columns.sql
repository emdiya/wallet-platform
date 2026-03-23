alter table transfers add column from_account_number varchar(20);
alter table transfers add column to_account_number varchar(20);
alter table transfers add column reference_no varchar(80);
alter table transfers add column purpose varchar(255);
alter table transfers add column completed_at timestamp;

update transfers
set from_account_number = '85501' || lpad(from_user_id::text, 10, '0'),
    to_account_number = '85501' || lpad(to_user_id::text, 10, '0'),
    reference_no = 'TRX-LEGACY-' || request_id
where from_account_number is null
   or to_account_number is null
   or reference_no is null;

alter table transfers alter column from_account_number set not null;
alter table transfers alter column to_account_number set not null;
alter table transfers alter column reference_no set not null;
alter table transfers add constraint uq_transfers_reference_no unique (reference_no);
