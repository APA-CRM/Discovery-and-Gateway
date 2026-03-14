package com.crm.gateway.filters;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

class AuthFilterTest extends BaseGatewayFilterIntegrationTest {

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void shouldReturn401WhenJwtMissing() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/organizations")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", is("Unauthorized"));
    }

    @Test
    @DisplayName("Should forward request when valid JWT token")
    public void shouldForwardRequestWhenJwtValid() {

        String accessToken = super.generateToken(1L, "login");

        wireMock.stubFor(get(urlEqualTo("/api/organizations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK")));

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, super.getAuthorizationHeaderValue(accessToken))
                .when()
                .get("/api/organizations")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

    }

}