package com.msn.msncars.company;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    Company fromDTO(CompanyDTO companyDTO);
    CompanyDTO toDTO(Company company);
}
