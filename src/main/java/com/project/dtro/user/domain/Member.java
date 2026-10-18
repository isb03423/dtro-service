package com.project.dtro.user.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String userid;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private MemberRole role = MemberRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public void updateProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateDevAccount(String encodedPassword, String name, MemberRole role) {
        this.password = encodedPassword;
        this.name = name;
        this.role = role;
        this.enabled = true;
        markUpdatedBy(this);
    }
}
