package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.OAuthAccount;
import com.microservices.auth.domain.entity.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

  Optional<OAuthAccount> findByProviderAndProviderUserId(
      OAuthProvider provider, String providerUserId);
}
