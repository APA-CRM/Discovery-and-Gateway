package com.crm.gateway.composite;

import com.crm.gateway.composite.clients.TasksClient;
import com.crm.gateway.composite.clients.UsersClient;
import com.crm.gateway.composite.dtos.request.TaskFilterRequest;
import com.crm.gateway.composite.dtos.request.TaskRequest;
import com.crm.gateway.composite.dtos.response.PagedResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskWithUsersResponse;
import com.crm.gateway.composite.dtos.response.users.UserLightResponse;
import com.crm.gateway.composite.mappers.TasksMapper;
import com.crm.gateway.composite.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TasksComposite {

    private final TasksClient tasksClient;
    private final UsersClient usersClient;

    private final UserDetailsService userDetailsService;

    private final TasksMapper tasksMapper;

    public Mono<PagedResponse<TaskWithUsersResponse>> filterTasks(
            Long organizationId, String authHeader, TaskFilterRequest request
    ) {
        return userDetailsService.getUserDetails(authHeader, organizationId)
                .flatMap(userDetails -> tasksClient.filterTasks(userDetails, request))
                .flatMap(pagedTasks -> {
                    Set<Long> usersIds = pagedTasks.getContent().stream()
                            .flatMap(task -> Stream.of(task.getAssignedTo(), task.getCreatedBy()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    return usersClient.getUsersByIds(usersIds)
                            .collectMap(UserLightResponse::getId)
                            .map(userMap -> {
                                List<TaskWithUsersResponse> combinedList = pagedTasks.getContent().stream()
                                        .map(task -> tasksMapper.toTaskWithUsersResponse(
                                                task, userMap.get(task.getAssignedTo()),
                                                userMap.get(task.getCreatedBy())
                                        ))
                                        .collect(Collectors.toList());

                                return new PagedResponse<>(combinedList, pagedTasks.getPage());
                            });
                });
    }

    public Mono<TaskWithUsersResponse> getTask(UUID taskId, Long organizationId, String authHeader) {
        return userDetailsService.getUserDetails(authHeader, organizationId)
                .flatMap(userDetails -> tasksClient.getTask(taskId, userDetails))
                .flatMap(this::addUsersToTask);
    }

    public Mono<TaskWithUsersResponse> createTask(TaskRequest request, Long organizationId, String authHeader) {
        return userDetailsService.getUserDetails(authHeader, organizationId)
                .flatMap(userDetails -> tasksClient.createTask(request, userDetails))
                .flatMap(this::addUsersToTask);
    }

    public Mono<TaskWithUsersResponse> updateTask(
            UUID taskId, TaskRequest request,
            Long organizationId, String authHeader
    ) {
        return userDetailsService.getUserDetails(authHeader, organizationId)
                .flatMap(userDetails -> tasksClient.updateTask(taskId, request, userDetails))
                .flatMap(this::addUsersToTask);
    }

    public Mono<ResponseEntity<Void>> deleteTask(UUID taskId, Long organizationId, String authHeader) {
        return userDetailsService.getUserDetails(authHeader, organizationId)
                .flatMap(userDetails -> tasksClient.deleteTask(taskId, userDetails));
    }

    private Mono<TaskWithUsersResponse> addUsersToTask(TaskResponse task) {
        Set<Long> userIds = Stream.of(task.getCreatedBy(), task.getAssignedTo())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return usersClient.getUsersByIds(userIds)
                .collectMap(UserLightResponse::getId)
                .map(usersMap -> tasksMapper.toTaskWithUsersResponse(
                        task, usersMap.get(task.getAssignedTo()),
                        usersMap.get(task.getCreatedBy())
                ));
    }

}
