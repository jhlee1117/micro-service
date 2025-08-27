package com.microservices.auth_service.service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservices.auth_service.dto.UserDto;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.dto.TenantDto;
import com.microservices.auth_service.domain.entity.User;
import com.microservices.auth_service.domain.entity.UserRole;
import com.microservices.auth_service.domain.entity.Role;
import com.microservices.auth_service.repository.UserRepository;
import com.microservices.auth_service.repository.UserRepositoryCustom;
import com.microservices.auth_service.repository.RoleRepository;
import com.microservices.auth_service.repository.UserRoleRepository;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRepositoryCustom userRepositoryCustom;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDto> getUserList() {
        return userRepositoryCustom.getUserList();
    }

    /**
     * 사용자 중복 검증
     */
    private void validateUserUniqueness(String username, String email, Long excludeUserId) {
        // Username 중복 검증 (업데이트 시에는 현재 사용자 제외)
        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent() && 
            (excludeUserId == null || !existingUserByUsername.get().getId().equals(excludeUserId))) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Email 중복 검증 (업데이트 시에는 현재 사용자 제외)
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent() && 
            (excludeUserId == null || !existingUserByEmail.get().getId().equals(excludeUserId))) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    /**
     * RoleDto들을 UserRole 엔티티들로 변환
     */
    private Set<UserRole> convertRoleDtosToUserRoles(Set<RoleDto> roleDtos, User user) {
        if (roleDtos == null || roleDtos.isEmpty()) {
            return new HashSet<>();
        }

        Set<UserRole> userRoles = new HashSet<>();
        
        for (RoleDto roleDto : roleDtos) {
            Optional<Role> roleOptional = roleRepository.findById(roleDto.getId());
            
            if (roleOptional.isPresent()) {
                Role role = roleOptional.get();
                
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(role)
                        .grantedBy(user)
                        .build();
                
                userRoles.add(userRole);
            } else {
                logger.warn("Role with ID {} not found", roleDto.getId());
            }
        }
        
        return userRoles;
    }

    /**
     * User 엔티티에서 RoleDto들을 추출
     */
    private Set<RoleDto> extractRoleDtosFromUser(User user) {
        Set<RoleDto> roleDtos = new HashSet<>();
        
        if (user.getUserRoles() != null) {
            for (UserRole ur : user.getUserRoles()) {
                if (ur.getRole() != null) {
                    RoleDto roleDto = RoleDto.fromEntity(ur.getRole());
                    if (roleDto != null) {
                        roleDtos.add(roleDto);
                    }
                }
            }
        }
        
        return roleDtos;
    }

    /**
     * User 엔티티에서 응답용 UserDto 생성
     */
    private UserDto createResponseDto(User user) {
        Set<RoleDto> roleDtos = extractRoleDtosFromUser(user);
        TenantDto tenantDto = TenantDto.fromEntity(user.getTenant());
        
        return new UserDto(
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            null, // password는 응답에 포함하지 않음
            user.isEnabled(),
            roleDtos,
            tenantDto
        );
    }
    
    /**
     * 기존 UserRole들과 새로운 RoleDto들을 비교하여 변경사항을 확인
     */
    private boolean hasRoleChanges(User existingUser, Set<RoleDto> newRoleDtos) {
        Set<Long> existingRoleIds = existingUser.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getId())
                .collect(Collectors.toSet());
        
        Set<Long> newRoleIds = newRoleDtos != null ? newRoleDtos.stream()
                .map(RoleDto::getId)
                .collect(Collectors.toSet()) : new HashSet<>();
        
        return !existingRoleIds.equals(newRoleIds);
    }
    
    /**
     * 사용자의 Role을 안전하게 업데이트하는 메서드
     */
    @Transactional
    private void updateUserRoles(User user, Set<RoleDto> newRoleDtos) {
        // 기존 UserRole들을 모두 삭제
        List<UserRole> existingUserRoles = userRoleRepository.findByUserId(user.getId());
        int deletedCount = existingUserRoles.size();
        
        if (deletedCount > 0) {
            userRoleRepository.deleteByUserId(user.getId());
            logger.info("Deleted {} existing roles for user: {}", deletedCount, user.getUsername());
        }
        
        // User 엔티티의 userRoles Set도 클리어
        user.getUserRoles().clear();
        
        // 새로운 Role들을 추가
        if (newRoleDtos != null && !newRoleDtos.isEmpty()) {
            Set<UserRole> newUserRoles = convertRoleDtosToUserRoles(newRoleDtos, user);
            
            for (UserRole userRole : newUserRoles) {
                // 중복 방지: 이미 존재하는지 체크
                if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), userRole.getRole().getId())) {
                    UserRole savedUserRole = userRoleRepository.save(userRole);
                    user.getUserRoles().add(savedUserRole);
                } else {
                    logger.warn("UserRole already exists for user {} and role {}", 
                        user.getUsername(), userRole.getRole().getName());
                }
            }
            
            logger.info("Added {} new roles for user: {}", newUserRoles.size(), user.getUsername());
        }
    }

    /**
     * 새 사용자 생성 시 User와 UserRole을 저장하는 로직
     */
    private User saveNewUserWithRoles(User user, Set<RoleDto> roleDtos) {
        // 사용자 먼저 저장 (UserRole 생성을 위해 User ID가 필요)
        User savedUser = userRepository.save(user);
        logger.info("Saved new user: {}", savedUser.getUsername());
        
        // RoleDto들을 UserRole로 변환하여 User에 추가
        Set<UserRole> userRoles = convertRoleDtosToUserRoles(roleDtos, savedUser);
        
        if (!userRoles.isEmpty()) {
            savedUser.setUserRoles(userRoles);
            savedUser = userRepository.save(savedUser);
            logger.info("Added {} roles to new user: {}", userRoles.size(), savedUser.getUsername());
        }
        
        return savedUser;
    }
    
    /**
     * 기존 사용자 업데이트 시 User와 UserRole을 저장하는 로직
     */
    private User saveUpdatedUserWithRoles(User user, Set<RoleDto> roleDtos) {
        // 사용자 정보 먼저 저장
        User savedUser = userRepository.save(user);
        logger.info("Updated user basic info: {}", savedUser.getUsername());
        
        // Role 변경사항이 있는 경우에만 Role 업데이트
        if (roleDtos == null || roleDtos.isEmpty()) {
            userRoleRepository.deleteByUserId(user.getId());
            logger.info("Delete roles for user: {}", savedUser.getUsername());
            return savedUser;
        }

        if (hasRoleChanges(savedUser, roleDtos)) {
            updateUserRoles(savedUser, roleDtos);
            // User 엔티티를 다시 조회하여 최신 UserRole 정보를 반영
            savedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);
            logger.info("Updated roles for user: {}", savedUser.getUsername());
        } else {
            logger.info("No role changes detected for user: {}", savedUser.getUsername());
        }
        
        return savedUser;
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        try {
            // 중복 사용자 검증
            validateUserUniqueness(userDto.getUsername(), userDto.getEmail(), null);
            
            // UserDto를 User 엔티티로 변환
            User user = User.builder()
                    .username(userDto.getUsername())
                    .email(userDto.getEmail())
                    .name(userDto.getName())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .tenant(userDto.getTenant() != null ? userDto.getTenant().toEntity() : null)
                    .enabled(userDto.isActive())
                    .build();
            
            // User와 Role들을 저장
            User savedUser = saveNewUserWithRoles(user, userDto.getRoles());
            
            // 응답 DTO 생성
            UserDto responseDto = createResponseDto(savedUser);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("사용자 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<Object> updateUser(String username, UserDto userDto) {
        try {
            // 기존 사용자 조회
            Optional<User> existingUserOpt = userRepository.findByUsername(username);
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found: " + username);
            }
            
            User existingUser = existingUserOpt.get();
            
            // 이메일 중복 검증만 수행 (username은 변경되지 않으므로)
            Optional<User> existingUserByEmail = userRepository.findByEmail(userDto.getEmail());
            if (existingUserByEmail.isPresent() && 
                !existingUserByEmail.get().getId().equals(existingUser.getId())) {
                throw new IllegalArgumentException("Email already exists: " + userDto.getEmail());
            }
            
            // 비밀번호 처리 (빈 값이면 기존 비밀번호 유지)
            String newPassword = (userDto.getPassword() != null && !userDto.getPassword().isBlank()) 
                ? passwordEncoder.encode(userDto.getPassword()) 
                : existingUser.getPassword();

            // 업데이트된 User 엔티티 생성
            User updatedUser = User.builder()
                    .id(existingUser.getId()) // 기존 ID 유지
                    .username(username) // username은 변경 불가
                    .email(userDto.getEmail())
                    .name(userDto.getName())
                    .password(newPassword)
                    .tenant(userDto.getTenant() != null ? userDto.getTenant().toEntity() : null)
                    .enabled(userDto.isActive())
                    .createdAt(existingUser.getCreatedAt()) // 기존 생성일 유지
                    .build();
            
            // User와 Role들을 저장
            User savedUser = saveUpdatedUserWithRoles(updatedUser, userDto.getRoles());
            
            // 응답 DTO 생성
            UserDto responseDto = createResponseDto(savedUser);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("사용자 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
