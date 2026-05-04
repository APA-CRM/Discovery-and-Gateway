package com.crm.gateway.composite.dtos.response.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusResponse {

    private Long id;

    private String name;

    private String color;

    private String type;

    private Instant createdAt;

    private Instant updatedAt;

}
