package com.microservices.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardRequestDto {
  @NotBlank(message = "Title is required") private String title;

  @NotBlank(message = "Content is required") private String content;
}
