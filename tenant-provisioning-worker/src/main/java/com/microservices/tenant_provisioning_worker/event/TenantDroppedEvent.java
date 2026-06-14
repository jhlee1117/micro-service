package com.microservices.tenant_provisioning_worker.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TenantDroppedEvent {

    private String eventId;
    private Long tenantId;
    private String tenantName;
    private LocalDateTime timestamp;
}
