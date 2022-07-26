package com.academy.accountservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpModel {

    @NotBlank(message = "Name should be not blank!")
    String name;
    @NotBlank(message = "Name should be not blank!")
    String lastname;
    @Email(message = "Email should be in proper format!")
    @Pattern(regexp = ".+@acme.com$", message = "Email should end with \"@acme.com\"!")
    @NotBlank(message = "Email should be not blank!")
    String email;
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @NotBlank(message = "Password should be not blank!")
    String password;
}
