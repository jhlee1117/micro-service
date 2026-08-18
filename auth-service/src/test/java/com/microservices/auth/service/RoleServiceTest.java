package com.microservices.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.common.exceptions.BusinessException;
import com.microservices.auth.domain.entity.Role;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import com.microservices.auth.repository.UserRoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RoleServiceTest {

  @Mock RoleRepository roleRepository;

  @Mock UserRoleRepository userRoleRepository;

  @Mock UserRepository userRepository;

  @InjectMocks RoleService roleService;

  @Nested
  @DisplayName("getRoleList")
  class GetRoleList {

    @Test
    void returns_all_roles_as_dtos() {
      // given
      List<Role> roles =
          List.of(
              Role.builder()
                  .id(1L)
                  .name("ROLE_ADMIN")
                  .description("Administrator")
                  .isSystemRole(true)
                  .build(),
              Role.builder()
                  .id(2L)
                  .name("ROLE_USER")
                  .description("User")
                  .isSystemRole(false)
                  .build());
      when(roleRepository.findAll()).thenReturn(roles);

      // when
      List<RoleDto> result = roleService.getRoleList();

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      assertThat(result.get(0).getName()).isEqualTo("ROLE_ADMIN");
      assertThat(result.get(0).getDescription()).isEqualTo("Administrator");
      assertThat(result.get(0).isSystemRole()).isTrue();
      assertThat(result.get(1).getId()).isEqualTo(2L);
      assertThat(result.get(1).getName()).isEqualTo("ROLE_USER");
      assertThat(result.get(1).isSystemRole()).isFalse();

      verify(roleRepository).findAll();
    }
  }

  @Nested
  @DisplayName("createRole")
  class CreateRole {

    @Test
    void creates_a_new_role() {
      // given
      RoleDto request = new RoleDto(null, "ROLE_MANAGER", "Manager role", false);
      Role savedRole =
          Role.builder()
              .id(1L)
              .name("ROLE_MANAGER")
              .description("Manager role")
              .isSystemRole(false)
              .build();

      when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(java.util.Optional.empty());
      when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

      // when
      RoleDto result = roleService.createRole(request);

      // then
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("ROLE_MANAGER");
      assertThat(result.getDescription()).isEqualTo("Manager role");
      assertThat(result.isSystemRole()).isFalse();

      ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
      verify(roleRepository).findByName("ROLE_MANAGER");
      verify(roleRepository).save(roleCaptor.capture());

      Role roleToSave = roleCaptor.getValue();
      assertThat(roleToSave.getName()).isEqualTo("ROLE_MANAGER");
      assertThat(roleToSave.getDescription()).isEqualTo("Manager role");
      assertThat(roleToSave.isSystemRole()).isFalse();
    }

    @Test
    void throws_exception_when_role_name_already_exists() {
      // given
      RoleDto request = new RoleDto(null, "ROLE_ADMIN", "Admin role", true);
      when(roleRepository.findByName("ROLE_ADMIN"))
          .thenReturn(java.util.Optional.of(Role.builder().id(1L).name("ROLE_ADMIN").build()));

      // when & then
      assertThatThrownBy(() -> roleService.createRole(request))
          .isInstanceOf(BusinessException.class);

      verify(roleRepository).findByName("ROLE_ADMIN");
      verify(roleRepository, never()).save(any(Role.class));
    }
  }

  @Nested
  @DisplayName("deleteRole")
  class DeleteRole {

    @Test
    @DisplayName("역할을 삭제한다")
    void delete_a_role() {
      // given
      Long roleId = 1L;
      String username = "superadmin";
      User requestUser = User.builder().id(10L).username(username).build();

      Role savedRole =
          Role.builder()
              .id(1L)
              .name("ROLE_TEST_MANAGER")
              .description("Manager Test role")
              .isSystemRole(false)
              .build();

      when(userRepository.findByUsername(username)).thenReturn(Optional.of(requestUser));
      when(userRoleRepository.existsByUserIdAndRoleNameAndSystemRole(
              requestUser.getId(), "ROLE_SUPER_ADMIN", true))
          .thenReturn(true);
      when(roleRepository.findById(roleId)).thenReturn(Optional.of(savedRole));

      // when
      roleService.deleteRole(roleId, username);

      // then
      verify(roleRepository).delete(savedRole);
    }

    @Test
    @DisplayName("존재하지 않는 역할을 삭제하지 못한다")
    void delete_a_role_when_role_does_not_exist() {
      // given
      Long roleId = 999L;
      String username = "superadmin";
      User requestUser = User.builder().id(10L).username(username).build();

      when(userRepository.findByUsername(username)).thenReturn(Optional.of(requestUser));
      when(userRoleRepository.existsByUserIdAndRoleNameAndSystemRole(
              requestUser.getId(), "ROLE_SUPER_ADMIN", true))
          .thenReturn(true);
      when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> roleService.deleteRole(roleId, username))
          .isInstanceOf(BusinessException.class);

      verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("슈퍼관리자 권한이 없으면 삭제하지 못한다")
    void delete_a_role_when_request_user_is_not_system_super_admin() {

      // given
      Long roleId = 1L;
      String username = "admin";
      User requestUser = User.builder().id(10L).username(username).build();

      when(userRepository.findByUsername(username)).thenReturn(Optional.of(requestUser));
      when(userRoleRepository.existsByUserIdAndRoleNameAndSystemRole(
              requestUser.getId(), "ROLE_SUPER_ADMIN", true))
          .thenReturn(false);

      // when & then
      assertThatThrownBy(() -> roleService.deleteRole(roleId, username))
          .isInstanceOf(BusinessException.class);

      verify(roleRepository, never()).findById(any());
      verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("요청 사용자가 존재하지 않으면 삭제하지 못한다")
    void delete_a_role_when_request_user_does_not_exist() {
      // given
      Long roleId = 1L;
      String username = "unknown";

      when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> roleService.deleteRole(roleId, username))
          .isInstanceOf(BusinessException.class);

      verify(userRoleRepository, never())
          .existsByUserIdAndRoleNameAndSystemRole(any(), any(), anyBoolean());
      verify(roleRepository, never()).findById(any());
      verify(roleRepository, never()).delete(any());
    }
  }
}
