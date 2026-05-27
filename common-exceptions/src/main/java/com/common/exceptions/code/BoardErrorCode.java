package com.common.exceptions.code;

import lombok.Getter;

@Getter
public enum BoardErrorCode implements ErrorCode {
    POST_NOT_FOUND("board.post.not_found", "게시글을 찾을 수 없습니다.", 404),
    NOT_AUTHORIZED("board.post.not_authorized", "작성자만 접근 가능합니다.", 403);

    private final String key;
    private final String message;
    private final int status;

    BoardErrorCode(String key, String message, int status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}
