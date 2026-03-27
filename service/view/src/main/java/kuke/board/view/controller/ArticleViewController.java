package kuke.board.view.controller;

import kuke.board.view.service.ArticleViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleViewController {
    private final ArticleViewService articleViewService;

    // 조회수 레디스 증가 및 100단위 백업 DB 업데이트 컨트롤러
    @PostMapping("/v1/article-views/articles/{articleId}/users/{userId}")
    public Long increase(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return articleViewService.increase(articleId, userId);
    }

    @GetMapping("/v1/article-views/articles/{articleId}/count")
    public Long count(
            @PathVariable("articleId") Long articleId
    ) {
        return articleViewService.count(articleId);
    }
}
