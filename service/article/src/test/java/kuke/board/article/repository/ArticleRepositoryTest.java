package kuke.board.article.repository;

import kuke.board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    // 전체 article 가져오기 테스트 - 페이징 버튼 형식 처리
    // boardId = 샤드 키
    // offset = 스킵 데이터 수
    // limit = 가져올 갯수
    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("article.size = {}", articles.size());
        for (Article article : articles){
            log.info("article = {}", article);
        }
    }

    @Test
    void countTest(){
        Long count = articleRepository.count(1L, 10000L);
        log.info("count = {}", count);
    }

    // 무한 스크롤 방식 페이징 쿼리 테스트
    @Test
    void findInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllInfiniteScroll(1L, 30L);
        for(Article article : articles){
            log.info("articleId = {}", article.getArticleId());
        }

        Long lastArticleId = articles.getLast().getArticleId();
        List<Article> articles2 = articleRepository.findAllInfiniteScroll(1L, 30L, lastArticleId);
        for(Article article : articles){
            log.info("articleId = {}", article.getArticleId());
        }
    }

}