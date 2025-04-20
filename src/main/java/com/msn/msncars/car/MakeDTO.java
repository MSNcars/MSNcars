package com.msn.msncars.car;

import java.util.List;
public record MakeDTO(
        Long id,
        String name,
        List<ModelDTO> models
){
}
