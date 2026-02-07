package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepositoryV2;

    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = this.findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepositoryV2.save(
                CommentV2.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommentPath.createChildCommentPath(
                                commentRepositoryV2.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        return CommentResponse.from(comment);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }
        return commentRepositoryV2.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepositoryV2.findById(commentId).orElseThrow());
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepositoryV2.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });

    }

    private boolean hasChildren(CommentV2 comment) {
        return commentRepositoryV2.findDescendantsTopPath(
                comment.getCommentId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepositoryV2.delete(comment);  // 현재 삭제하려는 댓글 삭제
        // 재귀적 부모 삭제
        if(!comment.isRoot()){
            // 삭제한 댓글이 부모 댓글이 아니라면
            commentRepositoryV2.findByPath(comment.getCommentPath().getPath())    // 부모 루트 댓글 찾고
                    .filter(CommentV2::getDeleted)                        // 루트 댓글이 삭제여부 true 인 댓글이면
                    .filter(Predicate.not(this::hasChildren))           // 루트 댓글의 다른 자식 댓글이 더 없다면
                    .ifPresent(this::delete);                           // 루트 댓글 삭제여부 true & 자식댓글 존재 X 인 댓글이 존재 한다면 이것도 삭제
        }
    }
}
