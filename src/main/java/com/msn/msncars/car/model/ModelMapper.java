package com.msn.msncars.car.model;


import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModelMapper {
    Model fromDTO(ModelDTO modelDTO);
    ModelDTO toDTO(Model model);
}