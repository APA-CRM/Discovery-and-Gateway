package com.crm.gateway.composite.clients;

import com.crm.gateway.composite.auth.UserDetails;
import com.crm.gateway.composite.dtos.request.TaskFilterRequest;
import com.crm.gateway.composite.dtos.response.tasks.TaskResponse;
import com.crm.gateway.composite.utils.FilterRequestToMapConvertor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_PERMISSIONS_HEADER_NAME;

@Component
public class TasksClient {

    protected static final String TASKS_FILTER = "/api/organizations/{organizationId}/tasks/filter";
    protected static final String TASKS_TASK_ID = "/api/organizations/{organizationId}/tasks/{taskId}";

    private final WebClient webClient;

    public TasksClient(@Qualifier("mainServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<PagedModel<TaskResponse>> filterTasks(
            UserDetails userDetails, TaskFilterRequest request
    ) {
        MultiValueMap<String, String> map = FilterRequestToMapConvertor.convert(request);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(TASKS_FILTER)
                        .queryParams(map)
                        .build(userDetails.organizationId())
                )
                .header(USER_ID_HEADER_NAME, userDetails.userId().toString())
                .header(USER_PERMISSIONS_HEADER_NAME, userDetails.permissions())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    public Mono<TaskResponse> getTask(UUID taskId, UserDetails userDetails) {
        return webClient.get()
                .uri(TASKS_TASK_ID, userDetails.organizationId(), taskId)
                .header(USER_ID_HEADER_NAME, userDetails.userId().toString())
                .header(USER_PERMISSIONS_HEADER_NAME, userDetails.permissions())
                .retrieve()
                .bodyToMono(TaskResponse.class);
    }

}
