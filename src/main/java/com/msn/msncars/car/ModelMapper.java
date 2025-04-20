package com.msn.msncars.car;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MakeMapper.class, componentModel = "spring")
public interface ModelMapper {
    ModelMapper INSTANCE = Mappers.getMapper(ModelMapper.class);

    Model fromDTO(ModelDTO modelDTO);
    ModelDTO toDTO(Model model);
}