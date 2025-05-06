package kuke.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "comment")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    private Long commentId;             // PK
    private String content;             // 댓글 내용
    private Long parentCommentId;       // 부모 댓글 PK
    private Long articleId;             // shard key
    private Long writerId;              // 작성자 PK
    private Boolean deleted;            // 삭제 여부
    private LocalDateTime createdAt;    // 댓글 생성 시간

    // Comment 생성 메서드
    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId){
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;    // 대댓글이면 넣어주고 아니면 본인 PK 넣어줌
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    // 루트 댓글 즉, 해당 댓글 뎁스의 최상위 댓글인지 판단 메서드
    public boolean isRoot() {
        return parentCommentId.longValue() == commentId;
    }

    // 댓글 삭제 여부 메서드
    public void delete() {
        deleted = true;
    }
}
