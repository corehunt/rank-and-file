create table raf_bill_text (
    text_id varchar(12) not null primary key,
    bill_id varchar(15) references raf_bill(bill_id),
    version_date date,
    version_type varchar(255),
    formatted_text_url text,
    pdf_url text,
    xml_url text,
    create_ts timestamp,
    update_ts timestamp
);