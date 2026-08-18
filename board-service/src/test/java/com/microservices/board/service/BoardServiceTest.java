package com.microservices.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microservices.board.domain.Board;
import com.microservices.board.dto.BoardRequestDto;
import com.microservices.board.dto.BoardResponseDto;
import com.microservices.board.repository.BoardRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

  @Mock private BoardRepository boardRepository;

  @InjectMocks private BoardService boardService;

  @Test
  @DisplayName("Create post successfully")
  void createPost_Success() {
    // given
    BoardRequestDto requestDto =
        BoardRequestDto.builder().title("Test Title").content("Test Content").build();
    String authorId = "test-user";
    Board board =
        Board.builder()
            .id(1L)
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .authorId(authorId)
            .build();

    when(boardRepository.save(any(Board.class))).thenReturn(board);

    // when
    BoardResponseDto responseDto = boardService.createPost(requestDto, authorId);

    // then
    assertThat(responseDto.getTitle()).isEqualTo(requestDto.getTitle());
    assertThat(responseDto.getContent()).isEqualTo(requestDto.getContent());
    assertThat(responseDto.getAuthorId()).isEqualTo(authorId);
    verify(boardRepository, times(1)).save(any(Board.class));
  }

  @Test
  @DisplayName("Get post successfully")
  void getPost_Success() {
    // given
    Long postId = 1L;
    Board board =
        Board.builder()
            .id(postId)
            .title("Test Title")
            .content("Test Content")
            .authorId("test-user")
            .build();

    when(boardRepository.findById(postId)).thenReturn(Optional.of(board));

    // when
    BoardResponseDto responseDto = boardService.getPost(postId);

    // then
    assertThat(responseDto.getId()).isEqualTo(postId);
    assertThat(responseDto.getTitle()).isEqualTo(board.getTitle());
  }
}
