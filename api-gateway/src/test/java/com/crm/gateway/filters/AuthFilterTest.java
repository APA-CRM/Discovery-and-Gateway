package com.crm.gateway.filters;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_LOGIN_HEADER_NAME;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

class AuthFilterTest extends BaseGatewayFilterIntegrationTest {

    @MockitoSpyBean
    private AuthFilter filter;

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

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Should forward request when valid JWT token")
    public void shouldForwardRequestWhenJwtValid() {

        long userId = 1L;
        String login = "login";
        String accessToken = super.generateToken(userId, login);

        wireMock.stubFor(get(urlEqualTo("/api/organizations"))
                .withHeader(USER_ID_HEADER_NAME, containing(String.valueOf(userId)))
                .withHeader(USER_LOGIN_HEADER_NAME, containing(login))
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

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

}