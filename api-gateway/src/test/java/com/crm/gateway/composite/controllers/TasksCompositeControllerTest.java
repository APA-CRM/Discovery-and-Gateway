package com.crm.gateway.composite.controllers;

import com.crm.gateway.composite.dtos.request.TaskRequest;
import com.crm.gateway.composite.dtos.response.PageMetadata;
import com.crm.gateway.composite.dtos.response.PagedResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskResponse;
import com.crm.gateway.composite.dtos.response.users.UserLightResponse;
import com.crm.gateway.filters.AuthFilter;
import com.crm.gateway.filters.AuthorizeAndCheckAccessFilter;
import com.crm.gateway.filters.BaseGatewayFilterIntegrationTest;
import com.crm.gateway.filters.OrganizationFilesFilter;
import com.crm.sharedlib.core.enums.Action;
import com.crm.sharedlib.core.enums.Resource;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import com.crm.sharedlib.rbac.dto.ResourcePermission;
import com.crm.sharedlib.rbac.dto.UserPermission;
import com.crm.sharedlib.rbac.utils.UserPermissionHeaderSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_PERMISSIONS_HEADER_NAME;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class TasksCompositeControllerTest extends BaseGatewayFilterIntegrationTest {

    @Autowired
    private List<GatewayFilter> gatewayFilters;

    @MockitoBean
    private AuthFilter authFilter;
    @MockitoBean
    private AuthorizeAndCheckAccessFilter accessFilter;
    @MockitoBean
    private OrganizationFilesFilter organizationFilesFilter;

    @Test
    @DisplayName("Get task with users expected success")
    public void getTaskWithUsers_thenSuccess() throws JsonProcessingException {

        final Long userId = 1L, organizationId = 1L, taskUserId = 1L;
        UUID taskId = UUID.randomUUID();

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        TaskResponse taskResponse = new TaskResponse();

        taskResponse.setId(taskId);
        taskResponse.setTitle("Test");
        taskResponse.setAssignedTo(taskUserId);
        taskResponse.setCreatedBy(taskUserId);

        wireMock.stubFor(get(urlEqualTo("/api/organizations/%d/tasks/%s".formatted(organizationId, taskId)))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(taskResponse))));

        // Stub Users API
        UserLightResponse userResponse = new UserLightResponse();

        userResponse.setId(taskUserId);
        userResponse.setFullName("Test User");

        wireMock.stubFor(get(urlPathEqualTo("/api/internal/users"))
                .withQueryParam("userId", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(userResponse)))));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .when()
                .get("/api/composite/organizations/{organizationId}/tasks/{taskId}", organizationId, taskId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(taskId.toString()))
                .body("title", is(taskResponse.getTitle()))
                .body("assignedTo", notNullValue())
                .body("createdBy", notNullValue());

    }

    @Test
    @DisplayName("Get task with users when task not exists expected not found")
    public void getTaskWithUsers_whenTaskNotExists_thenNotFound() throws JsonProcessingException {

        final Long userId = 1L, organizationId = 1L;
        UUID taskId = UUID.randomUUID();

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        CrmErrorResponse response = new CrmErrorResponse("Task is not found");

        wireMock.stubFor(get(urlEqualTo("/api/organizations/%d/tasks/%s".formatted(organizationId, taskId)))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(response))));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .when()
                .get("/api/composite/organizations/{organizationId}/tasks/{taskId}", organizationId, taskId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", is(response.getMessage()));

    }

    @Test
    @DisplayName("Filter task with users expected success")
    public void filterTaskWithUsers_thenSuccess() throws JsonProcessingException {

        final Long userId = 1L, organizationId = 1L, taskUserId = 1L;
        UUID taskId = UUID.randomUUID();

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        TaskResponse taskResponse = new TaskResponse();

        taskResponse.setId(taskId);
        taskResponse.setTitle("Test");
        taskResponse.setAssignedTo(taskUserId);
        taskResponse.setCreatedBy(taskUserId);

        PagedResponse<TaskResponse> pagedResponse = new PagedResponse<>(
                Collections.singletonList(taskResponse),
                new PageMetadata(10L, 0L, 1L, 1L)
        );

        wireMock.stubFor(get(urlPathEqualTo("/api/organizations/%d/tasks/filter".formatted(organizationId)))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .withQueryParam("sortDirection", equalTo("ASC"))
                .withQueryParam("sortBy", equalTo("id"))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(pagedResponse))));

        // Stub Users API
        UserLightResponse userResponse = new UserLightResponse();

        userResponse.setId(taskUserId);
        userResponse.setFullName("Test User");

        wireMock.stubFor(get(urlPathEqualTo("/api/internal/users"))
                .withQueryParam("userId", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(userResponse)))));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sortDirection", "ASC")
                .queryParam("sortBy", "id")
                .when()
                .get("/api/composite/organizations/{organizationId}/tasks/filter", organizationId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("content[0].id", is(taskId.toString()))
                .body("content[0].title", is(taskResponse.getTitle()))
                .body("content[0].assignedTo", notNullValue())
                .body("content[0].createdBy", notNullValue());

    }

    @Test
    @DisplayName("Create task and get it with users expected success")
    public void createTask_thenSuccess() throws JsonProcessingException {

        final Long userId = 1L, organizationId = 1L, taskUserId = 1L;
        UUID taskId = UUID.randomUUID();

        TaskRequest request = new TaskRequest();
        request.setTitle("Title");

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        TaskResponse taskResponse = new TaskResponse();

        taskResponse.setId(taskId);
        taskResponse.setTitle("Test");
        taskResponse.setAssignedTo(taskUserId);
        taskResponse.setCreatedBy(taskUserId);

        wireMock.stubFor(post(urlEqualTo("/api/organizations/%d/tasks".formatted(organizationId)))
                .withRequestBody(matchingJsonPath("$.title"))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(taskResponse))));

        // Stub Users API
        UserLightResponse userResponse = new UserLightResponse();

        userResponse.setId(taskUserId);
        userResponse.setFullName("Test User");

        wireMock.stubFor(get(urlPathEqualTo("/api/internal/users"))
                .withQueryParam("userId", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(userResponse)))));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .body(request)
                .when()
                .post("/api/composite/organizations/{organizationId}/tasks", organizationId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", is(taskId.toString()))
                .body("title", is(taskResponse.getTitle()))
                .body("assignedTo", notNullValue())
                .body("createdBy", notNullValue());

    }

    @Test
    @DisplayName("Update task and get it with users expected success")
    public void updateTask_thenSuccess() throws JsonProcessingException {

        final Long userId = 1L, organizationId = 1L, taskUserId = 1L;
        UUID taskId = UUID.randomUUID();

        TaskRequest request = new TaskRequest();
        request.setTitle("Title");

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        TaskResponse taskResponse = new TaskResponse();

        taskResponse.setId(taskId);
        taskResponse.setTitle("Test");
        taskResponse.setAssignedTo(taskUserId);
        taskResponse.setCreatedBy(taskUserId);

        wireMock.stubFor(put(urlEqualTo("/api/organizations/%d/tasks/%s".formatted(organizationId, taskId)))
                .withRequestBody(matchingJsonPath("$.title"))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(taskResponse))));

        // Stub Users API
        UserLightResponse userResponse = new UserLightResponse();

        userResponse.setId(taskUserId);
        userResponse.setFullName("Test User");

        wireMock.stubFor(get(urlPathEqualTo("/api/internal/users"))
                .withQueryParam("userId", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(userResponse)))));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .body(request)
                .when()
                .put("/api/composite/organizations/{organizationId}/tasks/{taskId}", organizationId, taskId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(taskId.toString()))
                .body("title", is(taskResponse.getTitle()))
                .body("assignedTo", notNullValue())
                .body("createdBy", notNullValue());

    }

    @Test
    @DisplayName("Delete task and get it with users expected success")
    public void deleteTask_thenSuccess() {

        final Long userId = 1L, organizationId = 1L;
        UUID taskId = UUID.randomUUID();

        // Stub Tasks API
        ResourcePermission resourcePermission = new ResourcePermission();
        resourcePermission.setResource(Resource.ALL);
        resourcePermission.setActions(Collections.singletonList(Action.ALL));

        UserPermission userPermission = new UserPermission(Collections.singletonList(resourcePermission));

        String userPermissionString = UserPermissionHeaderSerializer.serialize(userPermission);

        wireMock.stubFor(delete(urlEqualTo("/api/organizations/%d/tasks/%s".formatted(organizationId, taskId)))
                .withHeader(USER_ID_HEADER_NAME, equalTo(String.valueOf(userId)))
                .withHeader(USER_PERMISSIONS_HEADER_NAME, equalTo(userPermissionString))
                .willReturn(aResponse().withStatus(204)));

        applyDefaultBehaviorToFilter(gatewayFilters);

        given()
                .contentType(ContentType.JSON)
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_PERMISSIONS_HEADER_NAME, userPermissionString)
                .when()
                .delete("/api/composite/organizations/{organizationId}/tasks/{taskId}", organizationId, taskId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void applyDefaultBehaviorToFilter(List<GatewayFilter> gatewayFilters) {
        for (GatewayFilter gatewayFilter : gatewayFilters) {
            Mockito.when(gatewayFilter.filter(Mockito.any(), Mockito.any()))
                    .thenAnswer(invocation -> {
                        GatewayFilterChain chain = invocation.getArgument(1);
                        ServerWebExchange exchange = invocation.getArgument(0);

                        return chain.filter(exchange);
                    });
        }

    }

}