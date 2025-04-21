package com.msn.msncars.car.make;

/*
    Used when returning short information about Make, for example when returning information
    about make for particular listing.
 */
public record MakeSlimDTO(
        Long id,
        String name
){
}
