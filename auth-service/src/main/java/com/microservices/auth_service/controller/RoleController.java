package com.microservices.auth_service.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/role")
@Tag(name = "Role API", description = "역할 API 문서")
public class RoleController {

    @Autowired
    RoleService roleService;

    @GetMapping("/list")
    public List<RoleDto> getRoleList() {
        return roleService.getRoleList();
    }

}
