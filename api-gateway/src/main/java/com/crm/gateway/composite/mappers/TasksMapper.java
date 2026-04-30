package com.crm.gateway.composite.mappers;

import com.crm.gateway.composite.dtos.response.tasks.TaskResponse;
import com.crm.gateway.composite.dtos.response.tasks.TaskWithUsersResponse;
import com.crm.gateway.composite.dtos.response.users.UserLightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TasksMapper {

    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "createdBy", source = "createdBy")
    TaskWithUsersResponse toTaskWithUsersResponse(
            TaskResponse taskResponse,
            UserLightResponse assignedTo, UserLightResponse createdBy
    );

}
