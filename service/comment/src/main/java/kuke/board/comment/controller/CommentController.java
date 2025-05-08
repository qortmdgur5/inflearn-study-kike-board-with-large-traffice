package kuke.board.comment.controller;

import kuke.board.comment.service.CommentService;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Comment 조회 컨트롤러
    @GetMapping("/v1/comments/{commentId}")
    public CommentResponse read(
            @PathVariable("commentId") Long commentId
    ) {
        return commentService.read(commentId);
    }

    // Comment 생성 컨트롤러
    @PostMapping("/v1/comments")
    public CommentResponse create(@RequestBody CommentCreateRequest request) {
        return commentService.create(request);
    }

    // Comment 삭제 컨트롤러
    @DeleteMapping("/v1/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
    }

    // 페이징 버튼 형식 Comment 데이터 가져오기 컨트롤러
    @GetMapping("/v1/comments")
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentService.readAll(articleId, page, pageSize);
    }

    // 무한 스크롤 형식 Comment 데이터 가져오기 컨트롤러
    @GetMapping("/v1/comments/infinite-scroll")
    public List<CommentResponse> readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastParentCommentId", required = false) Long lastParentCommentId,
            @RequestParam(value = "lastCommentId", required = false) Long lastCommentId,
            @RequestParam("limit") Long limit
    ){
        return commentService.readAll(articleId, lastParentCommentId, lastCommentId, limit);
    }
}
