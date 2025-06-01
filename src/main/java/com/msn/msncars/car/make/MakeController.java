package com.msn.msncars.car.make;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/make")
public class MakeController {
    private final MakeService makeService;

    private final Logger logger = LoggerFactory.getLogger(MakeController.class);

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    @Operation(summary = "Get information about all makes without their models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Makes fetched successfully"),
    })
    @GetMapping("/all")
    public List<MakeSlimDTO> getAllMakes(){
        logger.info("Received request to get all makes.");

        List<MakeSlimDTO> makeSlimDTOs = makeService.getAllMakes();

        logger.info("All makes ({}) fetched successfully.", makeSlimDTOs.size());

        return makeSlimDTOs;
    }

    @Operation(summary = "Retrieve all makes along with their models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Makes with associated models fetched successfully"),
    })
    @GetMapping("/all/models")
    public List<MakeDTO> getAllMakesWithAssociatedModels(){
        logger.info("Received request to get all makes with associated models.");

        List<MakeDTO> makeDTOs = makeService.getAllMakesWithAssociatedModels();

        logger.info("All makes ({}) with associated models fetched successfully.", makeDTOs.size());

        return makeDTOs;
    }

    @Operation(summary = "Get information about a make by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Make information fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Make not found")
    })
    @GetMapping("/{makeName}")
    public MakeDTO getMakeInformation(@PathVariable String makeName){
        logger.info("Received request to get make information for make {}.", makeName);

        MakeDTO makeDTO = makeService.getMakeInformation(makeName);

        logger.info("Make information successfully retrieved.");

        return makeDTO;
    }
}
