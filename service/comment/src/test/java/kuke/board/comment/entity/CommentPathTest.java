package kuke.board.comment.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class CommentPathTest {
    @Test
    void createChildCommentTest() {
        // 00000 <- 생성 - 가장 첫 댓글
        createChildCommentTest(CommentPath.create(""), null, "00000");

        // 00000
        //      00000 <- 생성 - 00000 루트의 첫번째 대댓글 생성
        createChildCommentTest(CommentPath.create("00000"), null, "0000000000");

        // 00000
        // 00001 <- 생성 - 대댓글이 아닌 새로운 루트 댓글
        createChildCommentTest(CommentPath.create(""), "00000", "00001");

        // 0000z
        //      abcdz
        //           zzzzz
        //                zzzzz
        //      abce0 <- 생성
        createChildCommentTest(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzzz", "0000zabce0");

    }

    void createChildCommentTest(CommentPath commentPath, String descendantsTopPath, String expectedChildPath){
        CommentPath childCCommentPath = commentPath.createChildCommentPath(descendantsTopPath);
        assertThat(childCCommentPath.getPath()).isEqualTo(expectedChildPath);
    }

    // 5 최대 depth 초과 에러 테스트
    @Test
    void createChildCommentPathIfMaxDepthTest() {
        assertThatThrownBy(() ->
                CommentPath.create("zzzzz".repeat(5)).createChildCommentPath(null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // depth zzzzz 최대 변수 초과 에러 테스트
    @Test
    void createChildCommentPathIfChunkOverflowTest() {
        //given
        CommentPath commentPath = CommentPath.create("");

        // when, then
        assertThatThrownBy(() -> commentPath.createChildCommentPath("zzzzz"))
                .isInstanceOf(IllegalStateException.class);
    }

}