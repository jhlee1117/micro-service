package com.microservices.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.auth.config.TestQuerydslConfig;
import com.microservices.auth.domain.entity.AuthType;
import com.microservices.auth.domain.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(
    properties = {
      "spring.cloud.config.enabled=false",
      "spring.flyway.enabled=false",
      "eureka.client.enabled=false"
    })
@Import(TestQuerydslConfig.class)
@ActiveProfiles("test")
class UserRepositoryLockTest {

  @Autowired private UserRepository userRepository;

  @Test
  void findsSignupTokenWithPessimisticWriteLockOnH2() {
    User user =
        User.builder()
            .username("pending-oauth-user")
            .email("pending-oauth-user@example.com")
            .authType(AuthType.OAUTH)
            .signupCompleted(false)
            .signupTokenHash("signup-token-hash")
            .signupTokenExpiresAt(LocalDateTime.now().plusMinutes(10))
            .enabled(true)
            .build();

    userRepository.saveAndFlush(user);

    assertThat(userRepository.findBySignupTokenHash("signup-token-hash"))
        .isPresent()
        .get()
        .extracting(User::getUsername)
        .isEqualTo("pending-oauth-user");
  }
}
