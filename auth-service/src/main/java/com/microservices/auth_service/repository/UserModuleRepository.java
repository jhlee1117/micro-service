package com.microservices.auth_service.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.microservices.auth_service.domain.entity.UserModule;
import java.util.List;

@Repository
public interface UserModuleRepository extends JpaRepository<UserModule, Long> {
    List<UserModule> findByUserId(Long userId);
}
