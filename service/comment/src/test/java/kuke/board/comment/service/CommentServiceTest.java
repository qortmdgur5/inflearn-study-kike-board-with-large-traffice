package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;

    @Mock
    CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 댓글이 자식 있으면 삭제 표시만 ")
    void deleteShouldMarkDeletedIfHasChildren(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);  // 가자 mock 객체를 만들고

        // 가짜 mock comment 를 찾고
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));

        // 자식 댓글이 몇개 있는지 호출 - 자식 댓글이 있다고 가정
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L);

        // when
        // 자식댓글이 있을때를 가정하여 삭제 서비스 호출
        commentService.delete(commentId);

        // then
        // comment 객체의 자식댓글이 잇으므로 deleted = true 변환 메서드가 호출될 것이란 검증
        verify(comment).delete();
    }

    @Test
    @DisplayName("하위댓글 삭제 - 부모가 삭제되지 않았으면, 하위 댓글만 삭제한다.")
    void deleteShouldDeleteChildOnlyIfNotDeletedParent(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);  // parent 가 존재하는 가짜 mock 객체를 만들고
        given(comment.isRoot()).willReturn(false);  // 현재 삭제하려는 댓글이 root 댓글이 아닐때

        // 현재 삭제하려는 댓글의 부모 댓글은 deleted = false 가짜 객체 생성
        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false);

        // 가짜 mock comment 를 찾고
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        // 하위 댓글이 삭제 되었음을 가정 - 1L false 반환
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        // 가짜 mock parentComment 를 찾고
        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));

        // when
        // 하위 댓글 삭제 메서드 수행
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);  // comment 삭제 메서드는 실행 될 것
        verify(commentRepository, never()).delete(parentComment);   // parentComment 에 대한 delete 는 수행되지 않을 것
    }

    @Test
    @DisplayName("하위댓글 삭제 - 부모도 삭제라면, 재귀적으로 부모도 삭제")
    void deleteShouldDeleteAllRecursivelyIfDeletedParent(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);  // parent 가 존재하는 가짜 mock 객체를 만들고
        given(comment.isRoot()).willReturn(false);  // 현재 삭제하려는 댓글이 root 댓글이 아닐때

        // 현재 삭제하려는 댓글의 부모 댓글은 deleted = true 가짜 객체 생성, 부모도 삭제임
        Comment parentComment = createComment(articleId, parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        // 가짜 mock comment 를 찾고
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        // 하위 댓글이 삭제 되었음을 가정 - 1L false 반환
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        // 가짜 mock parentComment 를 찾고
        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));
        // 부모의 하위 댓글은 삭제 되었음 - 1L false 반환
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        // when
        // 하위 댓글 삭제 메서드 수행
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);  // comment 삭제 메서드는 실행 될 것
        verify(commentRepository).delete(parentComment);   // parentComment 에 대한 delete 는 수행되지 않을 것
    }

    private Comment createComment(Long articleId, Long commentId){
        Comment comment = mock(Comment.class);
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId){
        Comment comment = createComment(articleId, commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}