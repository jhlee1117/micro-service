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
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {
    
    @Id
    @Column(name = "menu_code", nullable = false, length = 10)
    private String menuCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_menu_code")
    private Menu parentMenu;
    
    @OneToMany(mappedBy = "parentMenu", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Menu> subMenus = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @Column(name = "path")
    private String path;
    
    @Column(name = "api_path")
    private String apiPath;
    
    @Column(name = "component")
    private String component;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "menu_alias")
    private String menuAlias;
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
