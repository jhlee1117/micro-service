package com.microservices.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microservices.auth.config.JpaAuditConfig;
import com.microservices.auth.config.TestQuerydslConfig;
import com.microservices.auth.domain.entity.Tenant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@Import({TestQuerydslConfig.class, JpaAuditConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.test.context.ActiveProfiles("test")
public class TenantRepositoryTest {

  @Autowired TenantRepository tenantRepository;

  @Test
  void testFindByName() throws Exception {

    var defaultName = "test";
    Tenant tenant = Tenant.builder().name(defaultName).status(true).build();

    tenantRepository.save(tenant);

    var result = tenantRepository.findByName("test");
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(defaultName);
  }

  @Test
  void testExistsByName() throws Exception {
    for (int i = 1; i < 5; i++) {
      Tenant tenant = Tenant.builder().name("test-" + i).status(true).build();

      tenantRepository.save(tenant);
    }

    var result = tenantRepository.existsByName("test-1");
    assertThat(result).isTrue();
    var falseResult = tenantRepository.existsByName("test-6");
    assertThat(falseResult).isFalse();
  }

  @Test
  void testSelectAll() throws Exception {
    // 기존 데이터 개수 확인 (초기 데이터 포함)
    var initialCount = tenantRepository.findAll().size();

    var newDataCount = 19; // 1부터 19까지
    List<String> expectedNames = new ArrayList<>();

    // 테스트 데이터 생성 및 저장
    for (int i = 1; i <= newDataCount; i++) {
      Tenant tenant = Tenant.builder().name("test-" + i).status(true).build();
      expectedNames.add("test-" + i);
      tenantRepository.save(tenant);
    }

    // 저장된 데이터 조회
    var result = tenantRepository.findAll();
    int totalCount = result.size();

    // 개수 검증 (초기 데이터 + 새로 추가한 데이터)
    assertEquals(initialCount + newDataCount, totalCount);

    // 새로 추가한 데이터만 필터링하여 검증
    List<String> actualNewNames =
        result.stream()
            .map(Tenant::getName)
            .filter(name -> name.startsWith("test-"))
            .sorted()
            .toList();

    List<String> sortedExpectedNames = expectedNames.stream().sorted().toList();

    assertEquals(sortedExpectedNames, actualNewNames, "The new test data should match");

    // 모든 엔티티가 저장되었는지 확인
    assertThat(result).hasSize(initialCount + newDataCount);
    assertThat(result)
        .allMatch(tenant -> tenant.getId() != null, "All tenants should have generated IDs");
    assertThat(result)
        .allMatch(
            tenant -> tenant.getCreatedAt() != null, "All tenants should have creation timestamps");
  }

  @Test
  void testCreateTenant() throws Exception {
    // 테스트 데이터 생성
    Tenant tenant = Tenant.builder().name("test-tenant").status(true).build();

    // 저장 전 상태 확인
    assertThat(tenant.getId()).isNull();

    // 저장 실행
    Tenant savedTenant = tenantRepository.save(tenant);

    // 저장 결과 검증
    assertThat(savedTenant.getId()).isNotNull();
    assertThat(savedTenant.getName()).isEqualTo("test-tenant");
    assertThat(savedTenant.isStatus()).isTrue();
    assertThat(savedTenant.getCreatedAt()).isNotNull();

    // 데이터베이스에서 조회하여 저장 확인
    var foundTenant = tenantRepository.findByName("test-tenant");
    assertThat(foundTenant).isPresent();
    assertThat(foundTenant.get().getId()).isEqualTo(savedTenant.getId());
  }
}
