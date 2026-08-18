package com.microservices.auth.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_type", nullable = false)
  @Builder.Default
  private AuthType authType = AuthType.LOCAL;

  @Column(name = "signup_completed", nullable = false)
  @Builder.Default
  private boolean signupCompleted = true;

  @Column(name = "signup_token_hash", nullable = true)
  private String signupTokenHash;

  @Column(name = "signup_token_expires_at", nullable = true)
  private LocalDateTime signupTokenExpiresAt;

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
    UserRole userRole = UserRole.builder().user(this).role(role).grantedBy(grantedBy).build();
    userRoles.add(userRole);
  }
}
