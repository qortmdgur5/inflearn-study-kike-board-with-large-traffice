package kuke.board.comment.service.response;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentV2;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class CommentResponse {
    private Long commentId;             // PK
    private String content;             // 댓글 내용
    private Long parentCommentId;       // 부모 댓글 PK
    private String path;                // 경로
    private Long articleId;             // shard key
    private Long writerId;              // 작성자 PK
    private Boolean deleted;            // 삭제 여부
    private LocalDateTime createdAt;    // 댓글 생성 시간

    // Comment 응답 객체 생성
    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.parentCommentId = comment.getParentCommentId();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.createdAt = comment.getCreatedAt();
        return response;
    }

    // CommentV2 응답 객체 생성
    public static CommentResponse from(CommentV2 comment) {
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.path = comment.getCommentPath().getPath();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.createdAt = comment.getCreatedAt();
        return response;
    }
}
