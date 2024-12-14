package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.SponsoredLegislationDTO;
import com.rankandfile.backend.entity.SponsoredLegislation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BillMapper.class})
public interface SponsoredLegislationMapper {

    @Mapping(target = "bill", source = "bill")
    SponsoredLegislationDTO toSponsoredLegislationDTO(SponsoredLegislation sponLeg);
}
