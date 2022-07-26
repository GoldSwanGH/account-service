package com.academy.accountservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassModel {

    @Size(min = 12, message = "The password length must be at least 12 chars!")
    String newPassword;
}
