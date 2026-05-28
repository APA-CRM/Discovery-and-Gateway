package com.crm.gateway;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class ActuatorCheckIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URI = "/actuator";

    @Test
    @DisplayName("Get /actuator/health endpoint expected success")
    public void getActuatorHealthEndpointExpectedSuccess() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_URI + "/health")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("status", is("UP"));
    }

    @Test
    @DisplayName("Get /actuator/health/readiness endpoint expected success")
    public void getActuatorHealthReadinessEndpointExpectedSuccess() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_URI + "/health/readiness")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("status", is("UP"));
    }

    @Test
    @DisplayName("Get /actuator/health/liveness endpoint expected success")
    public void getActuatorHealthLivenessEndpointExpectedSuccess() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_URI + "/health/liveness")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("status", is("UP"));
    }

}
