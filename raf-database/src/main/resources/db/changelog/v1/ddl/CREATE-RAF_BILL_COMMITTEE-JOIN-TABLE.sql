create table raf_bill_committee (
    bill_id varchar(15) references raf_bill(bill_id),
    committee_id varchar(20) references raf_committee(committee_id)
);
