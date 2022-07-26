package com.academy.accountservice.data.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "payments" ,uniqueConstraints = {
        @UniqueConstraint(name = "UniqueEmployeePeriod", columnNames = {"user_id", "period"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EmployeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User employee;

    private String period;

    private Long salary;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeePayment that = (EmployeePayment) o;
        return id.equals(that.id) && Objects.equals(employee, that.employee) && Objects.equals(period, that.period) && Objects.equals(salary, that.salary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employee, period, salary);
    }
}
