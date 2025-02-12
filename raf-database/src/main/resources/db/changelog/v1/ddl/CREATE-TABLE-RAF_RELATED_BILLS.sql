create table raf_bill_related_bills (
    bill_id varchar(50),
    related_bill_id varchar(50),
    primary key (bill_id, related_bill_id),
    foreign key (bill_id) references raf_bill(bill_id) on delete cascade,
    foreign key (related_bill_id) references raf_bill(bill_id) on delete cascade
);