create table raf_committee (
    committee_id varchar(20) primary key,
    chamber varchar(50),
    comm_type_cd varchar(50),
    comm_name varchar(255),
    sys_code varchar(50),
    url_src varchar(255),
    parent_id varchar(12),
    create_ts timestamp,
    update_ts timestamp,
    foreign key (parent_id) references raf_committee(committee_id)
);