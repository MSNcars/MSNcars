package com.msn.msncars.car.make;

import com.msn.msncars.car.model.ModelDTO;

import java.util.List;
public record MakeDTO(
        Long id,
        String name,
        List<ModelDTO> models
){
}
