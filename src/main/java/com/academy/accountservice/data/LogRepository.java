package com.academy.accountservice.data;

import com.academy.accountservice.data.entities.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    public List<Log> findAllByOrderByIdAsc();
}
