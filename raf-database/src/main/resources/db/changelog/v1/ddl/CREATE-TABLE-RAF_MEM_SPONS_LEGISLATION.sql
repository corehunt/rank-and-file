create table raf_spons_legislation (
    spon_leg_id varchar(12) primary key,
    person_id varchar(12) references raf_person(person_id),
    bill_id varchar(15) references raf_bill(bill_id),
    sponsor_type varchar(20) not null,
    create_ts timestamp,
    update_ts timestamp
);