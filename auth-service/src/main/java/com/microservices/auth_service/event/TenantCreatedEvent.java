package com.microservices.auth_service.event;

import java.time.LocalDateTime;
import java.util.UUID;
import com.microservices.auth_service.domain.entity.Tenant;
import com.microservices.auth_service.service.TenantService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantCreatedEvent {

    private String eventId;
    private Long tenantId;
    private String tenantName;
    private LocalDateTime timestamp;

    // 생성자
    public TenantCreatedEvent(Tenant savedTenant) {
        this.eventId = UUID.randomUUID().toString();
        this.tenantId = savedTenant.getId();
        this.tenantName = savedTenant.getName();
        this.timestamp = LocalDateTime.now();
    }
}
