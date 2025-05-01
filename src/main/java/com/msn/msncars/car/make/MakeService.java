package com.msn.msncars.car.make;

import java.util.List;
public interface MakeService {
    List<MakeSlimDTO>  getAllMakes();
    List<MakeDTO> getAllMakesWithAssociatedModels();
    MakeDTO getMakeInformation(String makeName);
}
