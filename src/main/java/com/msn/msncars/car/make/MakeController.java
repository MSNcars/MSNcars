package com.msn.msncars.car.make;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/make")
public class MakeController {
    private final MakeService makeService;

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    @GetMapping("/all")
    public List<MakeSlimDTO> getAllMakes(){
        return makeService.getAllMakes();
    }

    @GetMapping("/all/models")
    public List<MakeDTO> getAllMakesWithAssociatedModels(){
        return makeService.getAllMakesWithAssociatedModels();
    }

    @GetMapping("/{makeName}")
    public MakeDTO getMakeInformation(@PathVariable String makeName){
        return makeService.getMakeInformation(makeName);
    }
}
