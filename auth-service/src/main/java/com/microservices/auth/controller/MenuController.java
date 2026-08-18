package com.microservices.auth.controller;

import com.microservices.auth.dto.MenuDto;
import com.microservices.auth.service.MenuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@Tag(name = "Menu API", description = "메뉴 API 문서")
public class MenuController {

  private final MenuService menuService;

  @GetMapping("/list")
  public List<MenuDto> getMenuList() {
    return menuService.getMenuList();
  }

  @GetMapping("/{menuCode}")
  public ResponseEntity<MenuDto> getMenu(@PathVariable("menuCode") String menuCode) {
    return ResponseEntity.ok(menuService.getMenuByCode(menuCode));
  }

  @PostMapping("/create")
  public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
    return ResponseEntity.status(201).body(menuService.createMenu(menuDto));
  }

  @PutMapping("/{menuCode}")
  public ResponseEntity<MenuDto> updateMenu(
      @PathVariable("menuCode") String menuCode, @RequestBody MenuDto menuDto) {
    return ResponseEntity.ok(menuService.updateMenu(menuCode, menuDto));
  }

  @DeleteMapping("/{menuCode}")
  public ResponseEntity<Void> deleteMenu(@PathVariable("menuCode") String menuCode) {
    menuService.deleteMenu(menuCode);
    return ResponseEntity.noContent().build();
  }
}
