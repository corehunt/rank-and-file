create table raf_session (
    session_id bigint auto_increment primary key,
    chamber varchar(255) not null,
    number int not null,
    type varchar(255),
    start_date date,
    end_date date,
    congress_id int not null,
    create_ts timestamp,
    update_ts timestamp,
    foreign key (congress_id) references raf_congress(congress_id)
);