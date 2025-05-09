package com.msn.msncars.image;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageConfig {

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.username}")
    private String minioUsername;

    @Value("${minio.password}")
    private String minioPassword;

    @Bean
    MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioUsername, minioPassword)
                .build();
    }
}
