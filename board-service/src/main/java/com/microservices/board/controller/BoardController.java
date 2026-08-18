package com.microservices.board.controller;

import com.microservices.board.dto.BoardRequestDto;
import com.microservices.board.dto.BoardResponseDto;
import com.microservices.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

  private final BoardService boardService;

  @PostMapping
  public ResponseEntity<BoardResponseDto> createPost(
      @RequestHeader("X-User-Id") String authorId, @Valid @RequestBody BoardRequestDto requestDto) {
    return ResponseEntity.ok(boardService.createPost(requestDto, authorId));
  }

  @GetMapping
  public ResponseEntity<Page<BoardResponseDto>> getPosts(
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(boardService.getPosts(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BoardResponseDto> getPost(@PathVariable Long id) {
    return ResponseEntity.ok(boardService.getPost(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BoardResponseDto> updatePost(
      @RequestHeader("X-User-Id") String authorId,
      @PathVariable Long id,
      @Valid @RequestBody BoardRequestDto requestDto) {
    return ResponseEntity.ok(boardService.updatePost(id, requestDto, authorId));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(
      @RequestHeader("X-User-Id") String authorId, @PathVariable Long id) {
    boardService.deletePost(id, authorId);
    return ResponseEntity.noContent().build();
  }
}
