package com.microservices.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.dto.TenantDto;
import com.microservices.auth.event.TenantCreatedEvent;
import com.microservices.auth.repository.TenantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TenantServiceTest {

  @Mock private TenantRepository tenantRepository;

  @InjectMocks private TenantService tenantService;

  private List<Tenant> mockTenants;

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  @BeforeEach
  void setUp() {
    mockTenants = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      Tenant mockTenant =
          Tenant.builder()
              .id((long) i)
              .name("test_tenant_" + i)
              .status(i % 2 == 0) // 짝수는 true, 홀수는 false
              .build();
      mockTenants.add(mockTenant);
    }
  }

  @Test
  void testGetTenantList_성공() {
    // Given
    when(tenantRepository.findAll()).thenReturn(mockTenants);

    // When
    List<TenantDto> result = tenantService.getTenantList();

    // Then
    assertNotNull(result);
    assertEquals(5, result.size());

    // 첫 번째 테넌트 검증
    TenantDto firstTenant = result.get(0);
    assertEquals(1L, firstTenant.getId());
    assertEquals("test_tenant_1", firstTenant.getName());
    assertFalse(firstTenant.isStatus());

    // 두 번째 테넌트 검증
    TenantDto secondTenant = result.get(1);
    assertEquals(2L, secondTenant.getId());
    assertEquals("test_tenant_2", secondTenant.getName());
    assertTrue(secondTenant.isStatus());

    // Repository 메서드가 한 번 호출되었는지 검증
    verify(tenantRepository, times(1)).findAll();
  }

  @Test
  void testGetTenantList_빈리스트() {
    // Given
    when(tenantRepository.findAll()).thenReturn(new ArrayList<>());

    // When
    List<TenantDto> result = tenantService.getTenantList();

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tenantRepository, times(1)).findAll();
  }

  @Test
  void testGetTenantList_단일테넌트() {
    // Given
    List<Tenant> singleTenant =
        List.of(Tenant.builder().id(1L).name("single_tenant").status(true).build());
    when(tenantRepository.findAll()).thenReturn(singleTenant);

    // When
    List<TenantDto> result = tenantService.getTenantList();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());

    TenantDto tenantDto = result.get(0);
    assertEquals(1L, tenantDto.getId());
    assertEquals("single_tenant", tenantDto.getName());
    assertTrue(tenantDto.isStatus());

    verify(tenantRepository, times(1)).findAll();
  }

  @Test
  void testGetTenantList_Repository호출확인() {
    // Given
    when(tenantRepository.findAll()).thenReturn(mockTenants);

    // When
    tenantService.getTenantList();

    // Then
    verify(tenantRepository).findAll();
    verifyNoMoreInteractions(tenantRepository);
  }

  @Test
  void testCreateTenant_테넌트생성후스키마확인() {
    // Given
    TenantDto request = new TenantDto(null, "test_tenant", true);

    Tenant savedTenant = Tenant.builder().id(100L).name("test_tenant").status(true).build();

    when(tenantRepository.findByName("test_tenant")).thenReturn(Optional.empty());
    when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

    // When
    TenantDto result = tenantService.createTenant(request);

    // Then
    assertNotNull(result);
    assertEquals(100L, result.getId());
    assertTrue(result.isStatus());

    ArgumentCaptor<TenantCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(TenantCreatedEvent.class);

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    TenantCreatedEvent tenantCreatedEvent = eventCaptor.getValue();
    assertNotNull(tenantCreatedEvent.getEventId());
    assertNotNull(tenantCreatedEvent.getTimestamp());
    assertEquals(100L, tenantCreatedEvent.getTenantId());

    verify(tenantRepository, times(1)).findByName("test_tenant");
    verify(tenantRepository, times(1)).save(any(Tenant.class));
  }
}
