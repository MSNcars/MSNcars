package com.msn.msncars.car.make;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MakeMapper {
    Make fromSlimDTO(MakeSlimDTO makeSlimDTO);
    MakeSlimDTO toSlimDTO(Make make);

    Make fromDTO(MakeDTO makeDTO);
    MakeDTO toDTO(Make make);
}