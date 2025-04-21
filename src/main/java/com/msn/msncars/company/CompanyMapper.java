package com.msn.msncars.company;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company fromDTO(CompanyDTO companyDTO);
    CompanyDTO toDTO(Company company);
}
