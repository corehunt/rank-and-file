package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BillMapper {

    BillDTO toBillDTO(Bill bill);
}
