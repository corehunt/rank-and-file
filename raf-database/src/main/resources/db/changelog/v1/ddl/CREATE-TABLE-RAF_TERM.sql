create table raf_term (
    term_id serial primary key,
    person_id varchar(12) references raf_person(person_id),
    chamber varchar(100),
    congress integer,
    district integer,
    end_year integer,
    member_type varchar(100),
    start_year integer,
    state_code varchar(10),
    state_name varchar(100),
    create_ts timestamp,
    update_ts timestamp
);