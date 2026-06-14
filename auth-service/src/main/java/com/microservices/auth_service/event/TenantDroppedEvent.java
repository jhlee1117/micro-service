package com.microservices.auth_service.event;

import com.microservices.auth_service.domain.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantDroppedEvent {

    private String eventId;
    private Long tenantId;
    private String tenantName;
    private LocalDateTime timestamp;

    // 생성자
    public TenantDroppedEvent(Tenant savedTenant) {
        this.eventId = UUID.randomUUID().toString();
        this.tenantId = savedTenant.getId();
        this.tenantName = savedTenant.getName();
        this.timestamp = LocalDateTime.now();
    }
}
