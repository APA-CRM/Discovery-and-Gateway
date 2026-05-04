package com.crm.gateway.composite.dtos.response.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;

    private String title;

    private String description;

    private Integer estimatedTime;

    private TaskStatusResponse status;

    private TaskPriorityResponse priority;

    private Long assignedTo;

    private Long createdBy;

    private Instant dueDate;

    private Instant reminderAt;

    private Instant completedAt;

    private Instant createdAt;

    private Instant updatedAt;

}
