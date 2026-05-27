package com.microservices.board_service.service;

import com.common.exceptions.BusinessException;
import com.microservices.board_service.domain.Board;
import com.microservices.board_service.dto.BoardRequestDto;
import com.microservices.board_service.dto.BoardResponseDto;
import com.common.exceptions.code.BoardErrorCode;
import com.microservices.board_service.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public BoardResponseDto createPost(BoardRequestDto requestDto, String authorId) {
        Board board = Board.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .authorId(authorId)
                .build();
        return BoardResponseDto.from(boardRepository.save(board));
    }

    public Page<BoardResponseDto> getPosts(Pageable pageable) {
        return boardRepository.findAll(pageable).map(BoardResponseDto::from);
    }

    public BoardResponseDto getPost(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.POST_NOT_FOUND));
        return BoardResponseDto.from(board);
    }

    @Transactional
    public BoardResponseDto updatePost(Long id, BoardRequestDto requestDto, String authorId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.POST_NOT_FOUND));

        if (!board.getAuthorId().equals(authorId)) {
            throw new BusinessException(BoardErrorCode.NOT_AUTHORIZED);
        }

        board.update(requestDto.getTitle(), requestDto.getContent());
        return BoardResponseDto.from(board);
    }

    @Transactional
    public void deletePost(Long id, String authorId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.POST_NOT_FOUND));

        if (!board.getAuthorId().equals(authorId)) {
            throw new BusinessException(BoardErrorCode.NOT_AUTHORIZED);
        }

        boardRepository.delete(board);
    }
}
