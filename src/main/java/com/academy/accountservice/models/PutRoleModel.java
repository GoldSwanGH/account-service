package com.academy.accountservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PutRoleModel {

    @NotBlank
    String user;
    @NotBlank
    String role;
    @NotBlank
    String operation;
}
