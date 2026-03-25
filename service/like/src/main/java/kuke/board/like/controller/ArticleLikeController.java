package kuke.board.like.controller;

import kuke.board.like.service.ArticleLikeService;
import kuke.board.like.service.reponse.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ArticleLikeController {
    private final ArticleLikeService articleLikeService;

    // 해당 게시글 해당 유저가 좋아요 하였는지 체크 컨트롤러
    @GetMapping("/v1/article-likes/articles/{articleId}/users/{userId}")
    public ArticleLikeResponse read(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return articleLikeService.read(articleId, userId);
    }

    // 좋아요 수 가져오기 컨트롤러
    @GetMapping("/v1/article-likes/articles/{articleId}")
    public Long count(
            @PathVariable("articleId") Long articleId
    ) {
        return articleLikeService.count(articleId);
    }

    // 해당 게시글 해당 유저가 좋아요 수행 컨트롤러 - 비관적 락 1
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void likePessimisticLock1(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likePessimisticLock1(articleId, userId);
    }

    // 해당 게시글 해당 유저가 좋아요 취소 수행 컨트롤러 - 비관적 락 1
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void unlikePessimisticLock1(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikePessimisticLock1(articleId, userId);
    }

    // 해당 게시글 해당 유저가 좋아요 수행 컨트롤러 - 비관적 락 2
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void likePessimisticLock2(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likePessimisticLock2(articleId, userId);
    }

    // 해당 게시글 해당 유저가 좋아요 취소 수행 컨트롤러 - 비관적 락 2
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void unlikePessimisticLock2(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikePessimisticLock2(articleId, userId);
    }

    // 해당 게시글 해당 유저가 좋아요 수행 컨트롤러 - 낙관적 락
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void likeOptimisticLock(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likeOptimisticLock(articleId, userId);
    }

    // 해당 게시글 해당 유저가 좋아요 취소 수행 컨트롤러
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void unlikeOptimisticLock(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikeOptimisticLock(articleId, userId);
    }
}
