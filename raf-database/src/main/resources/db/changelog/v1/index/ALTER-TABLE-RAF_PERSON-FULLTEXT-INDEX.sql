alter table raf_person add fulltext index idx_raf_person_fulltext (
    first_nm,
    last_nm,
    full_nm,
    state,
    state_abbr,
    party_mem,
    party
);