package com.msn.msncars.car.model;

import com.msn.msncars.car.make.MakeSlimDTO;
public record ModelDTO(
        Long id,
        String name,
        MakeSlimDTO make
) {

}
