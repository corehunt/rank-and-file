create table raf_congress (
    congress_id int primary key,
    congress_name varchar(255),
    congress_number int not null,
    start_year varchar(255),
    end_year varchar(255),
    create_ts timestamp,
    update_ts timestamp
);