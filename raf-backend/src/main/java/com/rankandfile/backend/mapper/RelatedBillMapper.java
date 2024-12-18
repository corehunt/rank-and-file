package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.RelatedBillDTO;
import com.rankandfile.backend.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RelatedBillMapper {

    /**
     * Maps a Bill entity to a RelatedBillDTO.
     *
     * @param bill The Bill entity to map.
     * @return The corresponding RelatedBillDTO.
     */
    RelatedBillDTO toRelatedBillDTO(Bill bill);
}
