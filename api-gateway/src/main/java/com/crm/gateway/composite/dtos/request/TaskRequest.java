package com.crm.gateway.composite.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    private String title;

    private String description;

    private Integer estimatedTime;

    private Long statusId;

    private Long priorityId;

    private Long assignedTo;

    private Instant dueDate;

    private Instant reminderAt;

}
