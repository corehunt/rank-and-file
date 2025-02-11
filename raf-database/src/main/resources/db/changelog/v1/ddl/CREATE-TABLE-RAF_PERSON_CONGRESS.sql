create table raf_person_congress (
    person_id varchar(12) not null,
    congress_id int not null,
    primary key (person_id, congress_id),
    foreign key (person_id) references raf_person(person_id),
    foreign key (congress_id) references raf_congress(congress_id)
);