package com.microservices.auth_service.repository;

import java.util.List;
import com.microservices.auth_service.dto.UserDto;

public interface UserRepositoryCustom {

    List<UserDto> getUserList();

}
