package com.crm.gateway.composite.dtos.request;

import com.crm.sharedlib.core.dto.DateRange;
import com.crm.sharedlib.core.dto.request.BaseFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest extends BaseFilterRequest {

    private String title;

    private Long statusId;

    private Long priorityId;

    private Long assignedTo;

    private Long createdBy;

    private DateRange dueDate;

    private DateRange createdAt;

    private DateRange updatedAt;

}
