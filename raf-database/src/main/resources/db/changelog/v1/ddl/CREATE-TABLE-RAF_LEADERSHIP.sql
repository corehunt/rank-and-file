create table raf_leadership (
    leadership_id serial primary key,
    person_id varchar(12) references raf_person(person_id),
    congress varchar(3),
    leadership_type varchar(100),
    current_leader varchar(6),
    create_ts timestamp,
    update_ts timestamp
);