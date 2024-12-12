package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.SponsoredLegislationDTO;
import com.rankandfile.backend.entity.SponsoredLegislation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SponsoredLegislationMapper {

    // This method includes the bill if needed elsewhere:
    SponsoredLegislationDTO toSponsoredLegislationDTO(SponsoredLegislation sponLeg);

    // This method ignores the bill field:
    @Named("toSponsoredLegislationDTOWithoutBill")
    @Mapping(target = "bill", ignore = true)
    SponsoredLegislationDTO toSponsoredLegislationDTOWithoutBill(SponsoredLegislation sponLeg);
}
