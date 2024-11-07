package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.SponsoredLegislationDTO;
import com.rankandfile.backend.entity.SponsoredLegislation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BillMapper.class)
public interface SponsoredLegislationMapper {

    SponsoredLegislationDTO toSponsoredLegislationDTO(SponsoredLegislation sponLeg);
}
