package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.BillDTO;
import com.rankandfile.dataloader.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ActionMapper.class, SponsoredLegPersonMapper.class, CommitteeMapper.class})
public interface BillMapper {

    @Mapping(target = "sponsorships", source = "sponsorships")
    BillDTO toBillDTO(Bill bill);
}
