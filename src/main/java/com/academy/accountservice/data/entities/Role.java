package com.academy.accountservice.data.entities;

import com.academy.accountservice.data.RoleName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "roles" ,uniqueConstraints = {
        @UniqueConstraint(name = "UniqueUserRole", columnNames = {"username", "role"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleName role;
}

