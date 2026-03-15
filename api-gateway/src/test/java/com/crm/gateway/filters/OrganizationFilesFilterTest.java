package com.crm.gateway.filters;

import com.crm.gateway.clients.MainServiceClient;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.crm.sharedlib.core.consts.CrmHeaders.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

class OrganizationFilesFilterTest extends BaseGatewayFilterIntegrationTest {

    @MockitoSpyBean
    private OrganizationFilesFilter filter;

    @MockitoBean
    private UserPermissionService userPermissionService;
    @MockitoBean
    private MainServiceClient mainServiceClient;

    @Test
    @DisplayName("Should return 403 when File ID is missing")
    void shouldReturn403WhenFileIdIsMissing() {

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

        given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, super.getAuthorizationHeaderValue(accessToken))
                .header(ORGANIZATION_ID_HEADER_NAME, organizationId)
                .when()
                .get("/api/files/wrong-uri")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body("message", is("File ID not specified"));

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Should return forward request")
    void shouldReturnForwardRequest() {

        long userId = 1L;
        long organizationId = 1L;
        UUID fileId = UUID.fromString("4c65f07f-9b8e-44ec-a653-37dd192f724c");
        String login = "login";
        String accessToken = super.generateToken(userId, login);

        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.USERS);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        Mockito.when(userPermissionService.getUserPermission(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(userPermission));

        Mockito.when(mainServiceClient.checkFileExistenceInOrganization(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(ResponseEntity.of(Optional.empty())));

        wireMock.stubFor(get(urlEqualTo("/api/files/%s".formatted(fileId)))
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
                .get("/api/files/{fileId}", fileId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Mockito.verify(filter, Mockito.atLeastOnce()).filter(Mockito.any(), Mockito.any());
    }

}