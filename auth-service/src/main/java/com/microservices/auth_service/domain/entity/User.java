package com.microservices.auth_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "appuser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.experimental.SuperBuilder
public class User extends BaseAuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name")
    private String name;
    
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    @Builder.Default
    private AuthType authType = AuthType.LOCAL;

    @Column(name = "signup_completed", nullable = false)
    @Builder.Default
    private boolean signupCompleted = true;
        
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();
    
    public Set<String> getRoleNames() {
        Set<String> roleNames = new HashSet<>();
        for (UserRole userRole : userRoles) {
            roleNames.add(userRole.getRole().getName());
        }
        return roleNames;
    }
    
    public void addRole(Role role, User grantedBy) {
        UserRole userRole = UserRole.builder()
                .user(this)
                .role(role)
                .grantedBy(grantedBy)
                .build();
        userRoles.add(userRole);
    }
}
