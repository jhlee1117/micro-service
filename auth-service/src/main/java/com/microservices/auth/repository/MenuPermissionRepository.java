package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Menu;
import com.microservices.auth.domain.entity.MenuPermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermission, Long> {

  @Query(
      "select menuPermission from MenuPermission menuPermission "
          + "where menuPermission.menu = :menu order by menuPermission.permission.id")
  List<MenuPermission> findAllByMenuOrderByPermissionId(Menu menu);

  void deleteByMenu(Menu menu);
}
