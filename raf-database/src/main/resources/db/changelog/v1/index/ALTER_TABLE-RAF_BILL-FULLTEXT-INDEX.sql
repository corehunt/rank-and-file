alter table raf_bill add fulltext index idx_raf_bill_fulltext (
    bill_title,
    summary_txt,
    bill_no,
    bill_type,
    origin_chamber,
    policy_area,
    legislative_subjects,
    sponsors_txt
);
