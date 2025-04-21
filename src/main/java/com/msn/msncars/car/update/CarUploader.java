package com.msn.msncars.car.update;

import com.msn.msncars.car.make.Make;
import com.msn.msncars.car.make.MakeRepository;
import com.msn.msncars.car.model.Model;
import com.msn.msncars.car.model.ModelRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class CarUploader {

    private final ModelRepository modelRepository;
    private final MakeRepository makeRepository;
    private final RestClient vehicleApiClient;
    private final Logger logger = LoggerFactory.getLogger(CarUploader.class);

    public CarUploader(ModelRepository modelRepository, MakeRepository makeRepository, RestClient vehicleApiClient) {
        this.modelRepository = modelRepository;
        this.makeRepository = makeRepository;
        this.vehicleApiClient = vehicleApiClient;
    }

    /*
        Populates Model and Make tables in our database. It does not update Models and Makes already present in our database,
        instead it only inserts models/makes which IDs are completely missing.
    */
    //@Scheduled(cron = "0 0 0 * * ?") // every day at midnight
    @Scheduled(fixedRate = 10 * 1000 * 60) // every 10 minutes -> used for testing only
    public void updateVehicleInformationFromApi(){
        logger.info("Starting to update Vehicle data using external API.");

        var vehicleApiResponse = vehicleApiClient.get()
                .uri("/getmodelsformake/*?format=json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(AllModelsApiResponse.class);

        if(vehicleApiResponse == null || vehicleApiResponse.vehicles() == null){
            logger.info("External API returned empty data.");
            return;
        }

        if(vehicleApiResponse.count() == modelRepository.count()){
            logger.info("External API contains same number of cars as our database. Will not update database.");
            return;
        }

        for (VehicleInformation vehicleInformation : vehicleApiResponse.vehicles()){
            if(modelRepository.count() == 1000) break; //Insert only first 1000 vehicles -> used for testing only
            updateVehicleInformation(vehicleInformation);
        }

        logger.info("Vehicle data successfully updated.");
    }

    @Transactional
    private void updateVehicleInformation(VehicleInformation vehicleInformation){
        Optional<Model> modelOptional = modelRepository.findById(vehicleInformation.modelID());

        if(modelOptional.isPresent()){
            return;
        }

        Model model = new Model(vehicleInformation.modelID(), vehicleInformation.modelName());

        Optional<Make> makeOptional = makeRepository.findById(vehicleInformation.makeID());
        if(makeOptional.isPresent()){
            model.setMake(makeOptional.get());
        }else{
            model.setMake(
                    new Make(
                            vehicleInformation.makeID(),
                            vehicleInformation.makeName(),
                            List.of(model)
                    )
            );
        }

        modelRepository.save(model);
    }

}
