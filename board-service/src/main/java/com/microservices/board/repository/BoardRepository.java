package com.microservices.board.repository;

import com.microservices.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BoardRepository
    extends JpaRepository<Board, Long>, QuerydslPredicateExecutor<Board> {}
