package com.academy.accountservice.data;

import com.academy.accountservice.data.entities.EmployeePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {
}
