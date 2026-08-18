package com.microservices.auth.repository;

import com.microservices.auth.dto.UserDto;
import java.util.List;

public interface UserRepositoryCustom {

  List<UserDto> getUserList();
}
