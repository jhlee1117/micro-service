package com.microservices.tenantprovisioningworker.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreatedEvent {

  private String eventId;
  private Long tenantId;
  private String tenantName;
  private LocalDateTime timestamp;
}
