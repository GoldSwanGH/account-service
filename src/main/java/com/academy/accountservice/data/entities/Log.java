package com.academy.accountservice.data.entities;

import com.academy.accountservice.logging.SecurityEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "logs")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.DATE)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private SecurityEvent action;

    private String subject;

    private String object;

    private String path;
}

