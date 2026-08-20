package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Module;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.TenantModule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

  @Query(
      "select tenantModule from TenantModule tenantModule "
          + "where tenantModule.tenant = :tenant order by tenantModule.module.id")
  List<TenantModule> findAllByTenantOrderByModuleId(Tenant tenant);

  @Query(
      "select tenantModule from TenantModule tenantModule "
          + "where tenantModule.module = :module order by tenantModule.tenant.id")
  List<TenantModule> findAllByModuleOrderByTenantId(Module module);

  void deleteByTenant(Tenant tenant);

  void deleteByModule(Module module);
}
