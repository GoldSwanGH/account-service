package com.academy.accountservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePaymentModel {

    @NotBlank
    String employee;
    @Pattern(regexp = "(0[1-9]|1[0-2])-(19\\d{2}|20\\d{2})$")
    String period;
    @Min(0)
    Long salary;
}
