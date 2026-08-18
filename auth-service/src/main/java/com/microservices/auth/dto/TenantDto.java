package com.microservices.auth.dto;

import com.microservices.auth.domain.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {

  private Long id;
  private String name;
  private boolean status;

  // Tenant 엔티티에서 TenantDto로 변환하는 정적 메서드
  public static TenantDto fromEntity(Tenant tenant) {
    if (tenant == null) {
      return null;
    }
    return new TenantDto(tenant.getId(), tenant.getName(), tenant.isStatus());
  }

  // TenantDto에서 Tenant 엔티티로 변환하는 정적 메서드
  public Tenant toEntity() {
    return Tenant.builder().id(this.id).name(this.name).status(this.status).build();
  }
}
