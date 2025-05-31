package com.msn.msncars.gatling.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CompanyRegistrationScenario {

    private static int currentUserNumber = 1;
    private static int companyNumber = 1;
    private static final String COMPANY_TEST_USER_USERNAME_PREFIX = "performance_test_company_user__";
    private static final String COMPANY_NAME_PREFIX = "performance_test_company__";

    public static final ChainBuilder registerCompany =
            feed(CompanyRegistrationScenario::userRegistrationRequestIterator)
            .feed(CompanyRegistrationScenario::companyCreationRequestIterator)
                    .exec(
                            http("Company Registration")
                                    .post("http://localhost:80/auth/company/register")
                                    .header("Content-Type", "application/json")
                                    .body(CoreDsl.StringBody("""
                                            {
                                            "userRegistrationRequest": {
                                                "username": "#{username}",
                                                "password": "#{password}",
                                                "email": "#{userEmail}",
                                                "firstName": "#{firstName}",
                                                "lastName": "#{lastName}"
                                            },
                                            "companyCreationRequest": {
                                                "name": "#{companyName}",
                                                "address": "#{address}",
                                                "phone": "#{phone}",
                                                "email": "#{companyEmail}"
                                            }
                                            }
                                            """)
                                    )
                                    .check(status().is(201))
                                    .check(jsonPath("$.userId").saveAs("userId"))
                                    .check(jsonPath("$.companyId").saveAs("companyId"))
                    );

    private static Map<String, Object> createUserRegistrationRequest() {
        Map<String, Object> userData = new HashMap<>();
        String username = COMPANY_TEST_USER_USERNAME_PREFIX + currentUserNumber;
        userData.put("username", username);
        userData.put("password", "testPassword123");
        userData.put("userEmail", username + "@test.com");
        userData.put("firstName", username);
        userData.put("lastName", username);
        currentUserNumber++;
        return userData;
    }

    private static Map<String, Object> createCompanyCreationRequest() {
        Map<String, Object> companyData = new HashMap<>();
        String companyName = COMPANY_NAME_PREFIX + companyNumber;
        companyData.put("companyName", companyName);
        companyData.put("address", "Sample Address");
        companyData.put("phone", "123456789");
        companyData.put("companyEmail", companyName + "@test.com");
        companyNumber++;
        return companyData;
    }

    private static Iterator<Map<String, Object>> userRegistrationRequestIterator() {
        return Stream.generate(CompanyRegistrationScenario::createUserRegistrationRequest).iterator();
    }

    private static Iterator<Map<String, Object>> companyCreationRequestIterator() {
        return Stream.generate(CompanyRegistrationScenario::createCompanyCreationRequest).iterator();
    }

}
