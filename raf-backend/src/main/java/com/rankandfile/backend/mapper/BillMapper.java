package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ActionMapper.class, SponsoredLegPersonMapper.class})
public interface BillMapper {

    @Mapping(target = "sponsorships", source = "sponsorships")
    BillDTO toBillDTO(Bill bill);
}
