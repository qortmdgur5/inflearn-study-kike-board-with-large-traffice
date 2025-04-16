package kuke.board.article.controller;

import kuke.board.article.service.ArticleService;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    // articleId 에 해당 하는 Article 조회
    @GetMapping("/v1/articles/{articleId}")
    public ArticleResponse read(@PathVariable(name = "articleId") Long articleId){
        return articleService.read(articleId);
    }

    @GetMapping("/v1/articles")
    public ArticlePageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleService.readAll(boardId, page, pageSize);
    }

    // Article 전체 조회
    @PostMapping("/v1/articles")
    public ArticleResponse create(@RequestBody ArticleCreateRequest request){
        return articleService.create(request);
    }

    // articleId 에 해당 하는 정보 수정
    @PutMapping("/v1/articles/{articleId}")
    public ArticleResponse update(@PathVariable(name = "articleId") Long articleId,@RequestBody ArticleUpdateRequest request){
        return articleService.update(articleId, request);
    }

    // articleId 삭제
    @DeleteMapping("/v1/articles/{articleId}")
    public void delete(@PathVariable(name = "articleId") Long articleId){
        articleService.delete(articleId);
    }


}
