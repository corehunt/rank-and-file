ALTER TABLE RAF_PERSON ADD FULLTEXT INDEX idx_raf_person_fulltext (
    FIRST_NM,
    LAST_NM,
    FULL_NM,
    STATE,
    STATE_ABBR,
    PARTY_MEM,
    PARTY
);