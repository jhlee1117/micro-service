package com.microservices.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.TenantDto;
import com.microservices.auth.service.TenantService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TenantController.class)
public class TenantControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @MockitoBean private TenantService tenantService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void testGetTenantList_성공_데이터있음() throws Exception {
    // Given
    List<TenantDto> tenantDtoList = createMockTenantDtoList();
    when(tenantService.getTenantList()).thenReturn(tenantDtoList);

    // When & Then
    mockMvc
        .perform(get("/tenant/list"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("테넌트1"))
        .andExpect(jsonPath("$[0].status").value(true))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].name").value("테넌트2"))
        .andExpect(jsonPath("$[1].status").value(false))
        .andExpect(jsonPath("$[2].id").value(3L))
        .andExpect(jsonPath("$[2].name").value("테넌트3"))
        .andExpect(jsonPath("$[2].status").value(true));
  }

  @Test
  void testGetTenantList_성공_빈리스트() throws Exception {
    // Given
    List<TenantDto> emptyList = new ArrayList<>();
    when(tenantService.getTenantList()).thenReturn(emptyList);

    // When & Then
    mockMvc
        .perform(get("/tenant/list"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void testGetTenantList_단일테넌트() throws Exception {
    // Given
    List<TenantDto> singleTenantList = List.of(new TenantDto(1L, "단일테넌트", true));
    when(tenantService.getTenantList()).thenReturn(singleTenantList);

    // When & Then
    mockMvc
        .perform(get("/tenant/list"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("단일테넌트"))
        .andExpect(jsonPath("$[0].status").value(true));
  }

  @Test
  void testGetTenantList_서비스예외발생() throws Exception {
    // Given
    when(tenantService.getTenantList()).thenThrow(new RuntimeException("데이터베이스 연결 오류"));

    // When & Then
    mockMvc.perform(get("/tenant/list")).andExpect(status().isInternalServerError());
  }

  @Test
  void testGetTenantList_잘못된HTTP메서드() throws Exception {
    // When & Then
    mockMvc.perform(post("/tenant/list")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void testGetTenantList_잘못된URL() throws Exception {
    // When & Then
    mockMvc.perform(get("/tenant/invalid")).andExpect(status().is5xxServerError());
  }

  @Test
  void testGetTenantList_JSON응답구조검증() throws Exception {
    // Given
    List<TenantDto> tenantDtoList = createMockTenantDtoList();
    when(tenantService.getTenantList()).thenReturn(tenantDtoList);

    // When & Then
    mockMvc
        .perform(get("/tenant/list"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].id").exists())
        .andExpect(jsonPath("$[*].name").exists())
        .andExpect(jsonPath("$[*].status").exists())
        .andExpect(jsonPath("$[*].id").isArray())
        .andExpect(jsonPath("$[*].name").isArray())
        .andExpect(jsonPath("$[*].status").isArray());
  }

  private List<TenantDto> createMockTenantDtoList() {
    List<TenantDto> tenantDtoList = new ArrayList<>();
    tenantDtoList.add(new TenantDto(1L, "테넌트1", true));
    tenantDtoList.add(new TenantDto(2L, "테넌트2", false));
    tenantDtoList.add(new TenantDto(3L, "테넌트3", true));
    return tenantDtoList;
  }
}
