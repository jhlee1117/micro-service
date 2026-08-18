package com.microservices.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.service.RoleService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

  @Autowired WebApplicationContext webApplicationContext;

  @Autowired private MockMvc mockMvc;

  @MockitoBean RoleService roleService;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void testGetRoleDtoList_성공_데이터있음() throws Exception {

    when(roleService.getRoleList()).thenReturn(createMockRoleDtoList());

    mockMvc
        .perform(get("/role/list"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[1].id").value(2L));
  }

  @Test
  void testDeleteRole_성공() throws Exception {
    Long roleId = 1L;
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("superadmin", null);

    mockMvc
        .perform(delete("/role/{roleId}", roleId).principal(authentication))
        .andExpect(status().isNoContent());

    verify(roleService).deleteRole(roleId, "superadmin");
  }

  private List<RoleDto> createMockRoleDtoList() {
    List<RoleDto> roleDtoList = new ArrayList<>();
    roleDtoList.add(new RoleDto(1L, "ADMIN", "관리자용", Boolean.TRUE));
    roleDtoList.add(new RoleDto(2L, "USER", "사용자용", Boolean.TRUE));

    return roleDtoList;
  }
}
