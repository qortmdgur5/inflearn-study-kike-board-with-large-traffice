package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    // Comment 생성 서비스
    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getParentCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    // 부모 댓글 찾기 메서드
    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if(parentCommentId == null){
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .filter(Predicate.not(Comment::getDeleted)) // deleted 삭제 여부가 false 인 것만 통과 - not 은 boolean 반전
                .filter(Comment::isRoot)                    // 최대 2depth 구성 - root 인 부모댓글인 경우만
                .orElseThrow();
    }

    // 조회 서비스
    public CommentResponse read(Long commentId){
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    // 댓글 삭제 서비스
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(Predicate.not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)){
                        comment.delete();
                    }else{
                        delete(comment);
                    }
                });
    }

    // 대댓글을 가지고 있는지 판별 메서드
    // parentCommentId 로 자기 자신의 commentId 를 날리면 자기 자신이 root 라는 가정 하에 자신과 자식 2개까지만 조회해도 대댓글이 있다는 판명나니까
    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    // 댓글의 재귀적 완전 삭제 메서드 - 삭제한 댓글의 부모가 자식이 없을때 재귀적으로 삭제하기 위함 2depth 여서 가능한 현재 로직임
    private void delete(Comment comment) {
        commentRepository.delete(comment);  // 현재 삭제하려는 댓글 삭제
        // 재귀적 부모 삭제
        if(!comment.isRoot()){
            // 삭제한 댓글이 부모 댓글이 아니라면
            commentRepository.findById(comment.getParentCommentId())    // 부모 루트 댓글 찾고
                    .filter(Comment::getDeleted)                        // 루트 댓글이 삭제여부 true 인 댓글이면
                    .filter(Predicate.not(this::hasChildren))           // 루트 댓글의 다른 자식 댓글이 더 없다면
                    .ifPresent(this::delete);                           // 루트 댓글 삭제여부 true & 자식댓글 존재 X 인 댓글이 존재 한다면 이것도 삭제
        }
    }

    // Comment 페이징 처리 전체 데이터 가져오기 - 현재 페이지 영역 ex 1-10 페이지 의 전체 데이터 갯수 까지
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize){
        return CommentPageResponse.of(
                // 해당 페이지 Comment 댓글 전부 가져오기
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                // 해당 페이지 영역 1-10 의 전체 데이터 갯수
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    // Comment 무한 스크롤 형식 전체 데이터 가져오기
    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit){
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
            commentRepository.findAllInfiniteScroll(articleId, limit) :
            commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}
