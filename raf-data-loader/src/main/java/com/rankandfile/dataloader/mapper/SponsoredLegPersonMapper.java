package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.SponsoredLegPersonDTO;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SponsoredLegPersonMapper {

    @Mapping(target = "sponLegId", source = "sponLegId")
    @Mapping(target = "person", source = "person")
    @Mapping(target = "sponsorType", source = "sponsorType")
    SponsoredLegPersonDTO toSponsoredLegislationPersonDTO(SponsoredLegislation sponLeg);
}