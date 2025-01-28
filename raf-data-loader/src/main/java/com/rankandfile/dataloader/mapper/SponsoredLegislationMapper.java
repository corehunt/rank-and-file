package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.SponsoredLegislationDTO;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BillMapper.class})
public interface SponsoredLegislationMapper {

    @Mapping(target = "bill", source = "bill")
    SponsoredLegislationDTO toSponsoredLegislationDTO(SponsoredLegislation sponLeg);
}
