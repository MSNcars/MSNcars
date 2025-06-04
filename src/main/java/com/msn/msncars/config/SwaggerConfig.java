package com.msn.msncars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:80"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                ));
    }

    @Bean
    public OpenApiCustomizer externalLoginEndpoint() {
        return openApi ->
            openApi.path("/auth/login",
                    new PathItem().post(
                            new Operation()
                                    .summary("Login")
                                    .description("Send a request to obtain a JWT token. Required fields: username, password, grant_type, client_id.")
                                    .addTagsItem("Authentication")
                                    .requestBody(new RequestBody()
                                            .required(true)
                                            .description("Keycloak login request")
                                            .content(
                                                    new Content().addMediaType("application/x-www-form-urlencoded",
                                                        new MediaType()
                                                                .addEncoding("grant_type", new Encoding())
                                                                .addEncoding("client_id", new Encoding())
                                                                .addEncoding("username", new Encoding())
                                                                .addEncoding("password", new Encoding())
                                                                .schema(new Schema<>().type("object")
                                                                        .addProperty("grant_type", new StringSchema()._default("password"))
                                                                        .addProperty("client_id", new StringSchema()._default("MSNcars"))
                                                                        .addProperty("username", new StringSchema().example("user"))
                                                                        .addProperty("password", new StringSchema().example("user")))
                                                    )))
                                    .responses(new ApiResponses()
                                            .addApiResponse("200", new ApiResponse().description("Successful login"))
                                            .addApiResponse("401", new ApiResponse().description("Invalid credentials"))
                                    )
                    )
            );
    }

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation -> {

                if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                    ApiResponses responses = operation.getResponses();
                    responses.addApiResponse("401", new ApiResponse()
                            .description("Authentication required: please provide a valid access token."));
                }

            })
        );
    }
}
