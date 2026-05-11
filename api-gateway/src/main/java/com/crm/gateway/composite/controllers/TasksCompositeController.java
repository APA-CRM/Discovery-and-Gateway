package com.crm.gateway.composite.controllers;

import com.crm.gateway.composite.TasksComposite;
import com.crm.gateway.composite.dtos.request.TaskFilterRequest;
import com.crm.gateway.composite.dtos.request.TaskRequest;
import com.crm.gateway.composite.dtos.response.PagedResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskWithUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/composite/organizations")
@RequiredArgsConstructor
public class TasksCompositeController {

    private final TasksComposite tasksComposite;

    @GetMapping("/{organizationId}/tasks/filter")
    public Mono<PagedResponse<TaskWithUsersResponse>> filterTasks(
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(AUTHORIZATION) String authHeader,
            @ModelAttribute TaskFilterRequest request
    ) {
        return tasksComposite.filterTasks(organizationId, authHeader, request);
    }

    @GetMapping("/{organizationId}/tasks/{taskId}")
    public Mono<TaskWithUsersResponse> getTask(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(AUTHORIZATION) String authHeader
    ) {
        return tasksComposite.getTask(taskId, organizationId, authHeader);
    }

    @PostMapping("/{organizationId}/tasks")
    public Mono<TaskWithUsersResponse> createTask(
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(AUTHORIZATION) String authHeader,
            @RequestBody TaskRequest request
    ) {
        return tasksComposite.createTask(request, organizationId, authHeader);
    }

    @PutMapping("/{organizationId}/tasks/{taskId}")
    public Mono<TaskWithUsersResponse> updateTask(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(AUTHORIZATION) String authHeader,
            @RequestBody TaskRequest request
    ) {
        return tasksComposite.updateTask(taskId, request, organizationId, authHeader);
    }

    @DeleteMapping("/{organizationId}/tasks/{taskId}")
    public Mono<ResponseEntity<Void>> deleteTask(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(AUTHORIZATION) String authHeader
    ) {
        return tasksComposite.deleteTask(taskId, organizationId, authHeader);
    }


}
