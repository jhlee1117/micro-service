package com.microservices.board.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDto {
  private Long id;
  private String title;
  private String content;
  private String authorId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static BoardResponseDto from(com.microservices.board.domain.Board board) {
    return BoardResponseDto.builder()
        .id(board.getId())
        .title(board.getTitle())
        .content(board.getContent())
        .authorId(board.getAuthorId())
        .createdAt(board.getCreatedAt())
        .updatedAt(board.getUpdatedAt())
        .build();
  }
}
