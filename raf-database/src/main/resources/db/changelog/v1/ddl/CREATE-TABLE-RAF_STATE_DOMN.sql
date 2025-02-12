create table raf_state_domn (
    state_id char(3) not null,
    state_abbr char(2) not null primary key,
    state_nm varchar(50) not null,
    capital varchar(50) not null
);
