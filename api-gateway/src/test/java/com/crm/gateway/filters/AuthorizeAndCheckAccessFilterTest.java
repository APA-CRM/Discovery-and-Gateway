package com.crm.gateway.filters;

import com.crm.gateway.service.UserPermissionService;
import com.crm.sharedlib.core.enums.Action;
import com.crm.sharedlib.core.enums.Resource;
import com.crm.sharedlib.rbac.dto.ResourcePermission;
import com.crm.sharedlib.rbac.dto.UserPermission;
import com.crm.sharedlib.rbac.utils.UserPermissionHeaderSerializer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.crm.sharedlib.core.consts.CrmHeaders.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

class AuthorizeAndCheckAccessFilterTest extends BaseGatewayFilterIntegrationTest {

    @MockitoSpyBean
    private AuthorizeAndCheckAccessFilter filter;

    @MockitoBean
    private UserPermissionService userPermissionService;

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void shouldReturn401WhenJwtMissing() {

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/organizations/1/users/1")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("message", is("Unauthorized"));

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Should return 403 when Organization ID is missing")
    void shouldReturn403WhenOrganizationIdIsMissing() {

        long userId = 1L;
        String login = "login";
        String accessToken = super.generateToken(userId, login);

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, super.getAuthorizationHeaderValue(accessToken))
                .when()
                .delete("/api/files/1")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body("message", is("Forbidden"));

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Should forward request")
    void shouldForwardRequest() {

        long userId = 1L;
        long organizationId = 1L;
        String login = "login";
        String accessToken = super.generateToken(userId, login);

        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.USERS);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        Mockito.when(userPermissionService.getUserPermission(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(userPermission));

        wireMock.stubFor(get(urlEqualTo("/api/organizations/1/users/1"))
                .withHeader(USER_ID_HEADER_NAME, containing(String.valueOf(userId)))
                .withHeader(USER_LOGIN_HEADER_NAME, containing(login))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, containing(UserPermissionHeaderSerializer.serialize(userPermission)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK")));

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, super.getAuthorizationHeaderValue(accessToken))
                .header(ORGANIZATION_ID_HEADER_NAME, organizationId)
                .when()
                .get("/api/organizations/1/users/1")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }


}