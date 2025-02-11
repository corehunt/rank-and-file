create table raf_bill_action (
    action_id bigint auto_increment primary key,
    bill_id varchar(15) references raf_bill(bill_id),
    action_cd varchar(10),
    action_dt date,
    src_sys_cd int,
    src_nm varchar(50),
    action_txt text,
    action_type varchar(40),
    committee_ref varchar(2000),
    create_ts timestamp,
    update_ts timestamp
);