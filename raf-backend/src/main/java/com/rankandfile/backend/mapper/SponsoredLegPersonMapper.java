package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.SponsoredLegPersonDTO;
import com.rankandfile.backend.entity.SponsoredLegislation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SponsoredLegPersonMapper {

    @Mapping(target = "sponLegId", source = "sponLegId")
    @Mapping(target = "person", source = "person")
    @Mapping(target = "sponsorType", source = "sponsorType")
    SponsoredLegPersonDTO toSponsoredLegislationPersonDTO(SponsoredLegislation sponLeg);
}