package com.msn.msncars.car.update;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class VehicleApiConfiguration {

    @Bean
    RestClient vehicleApiClient(){
        return RestClient.create("https://vpic.nhtsa.dot.gov/api/vehicles");
    }

}
