package com.msn.msncars.car;

import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MakeMapper {
    Make fromSlimDTO(MakeSlimDTO makeSlimDTO);

    MakeSlimDTO toSlimDTO(Make make);

    Make fromDTO(MakeDTO makeDTO, @Context ModelMapper modelMapper);

    MakeDTO toDTO(Make make, @Context ModelMapper modelMapper);
}