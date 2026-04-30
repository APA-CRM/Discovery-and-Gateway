package com.crm.gateway.composite.dtos.response.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLightResponse {

    private Long id;

    private String login;

    private String email;

    private String fullName;

}