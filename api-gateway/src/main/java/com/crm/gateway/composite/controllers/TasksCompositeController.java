package com.crm.gateway.composite.controllers;

import com.crm.gateway.composite.TasksComposite;
import com.crm.gateway.composite.auth.UserDetails;
import com.crm.gateway.composite.dtos.request.TaskFilterRequest;
import com.crm.gateway.composite.dtos.response.PagedResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskWithUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_PERMISSIONS_HEADER_NAME;

@RestController
@RequestMapping("/api/composite/organizations")
@RequiredArgsConstructor
public class TasksCompositeController {

    private final TasksComposite tasksComposite;

    @GetMapping("/{organizationId}/tasks/filter")
    public Mono<PagedResponse<TaskWithUsersResponse>> filterTasks(
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(USER_ID_HEADER_NAME) Long userId,
            @RequestHeader(USER_PERMISSIONS_HEADER_NAME) String permissions,
            @ModelAttribute TaskFilterRequest request
    ) {
        return tasksComposite.filterTasks(
                new UserDetails(userId, organizationId, permissions),
                request
        );
    }

    @GetMapping("/{organizationId}/tasks/{taskId}")
    public Mono<TaskWithUsersResponse> getTask(
            @PathVariable("taskId") UUID taskId,
            @PathVariable("organizationId") Long organizationId,
            @RequestHeader(USER_ID_HEADER_NAME) Long userId,
            @RequestHeader(USER_PERMISSIONS_HEADER_NAME) String permissions
    ) {
        return tasksComposite.getTask(taskId, new UserDetails(userId, organizationId, permissions));
    }

}
