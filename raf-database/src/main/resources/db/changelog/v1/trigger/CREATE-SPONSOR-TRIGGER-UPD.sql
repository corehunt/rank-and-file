-- after update trigger
create trigger update_sponsors_txt_upd
    after update on raf_spons_legislation
    for each row
begin
    declare vbillid varchar(15);

    if (new.bill_id is not null) then
        set vbillid = new.bill_id;
    else
        set vbillid = old.bill_id;
    end if;

    update raf_bill
    set sponsors_txt = (
        select group_concat(p.full_nm separator ', ')
        from raf_spons_legislation sl
                 join raf_person p on sl.person_id = p.person_id
        where sl.bill_id = vbillid
    )
    where bill_id = vbillid;
end;