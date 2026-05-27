package com.microservices.auth_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.experimental.SuperBuilder
public class Module extends BaseAuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "url", nullable = false)
    private String url;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Menu> menus = new HashSet<>();
    
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TenantModule> tenantModules = new HashSet<>();
    
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserModule> userModules = new HashSet<>();

}
