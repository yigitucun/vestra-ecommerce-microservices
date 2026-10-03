package com.vestra.auth.service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vestra.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CUSTOMER;
    @CreationTimestamp
    private Instant createdAt;
    @Builder.Default
    private boolean isActive = true;
}
