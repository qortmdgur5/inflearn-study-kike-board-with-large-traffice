package kuke.board.article.api;

import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ArticleTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    // Article 생성 테스트 메서드
    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "my content", 1L, 1L
        ));
        System.out.println("response = " + response);
    }


    ArticleResponse create(ArticleCreateRequest request){
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()                     // 실제 서버로 통신을 하겠다
                .body(ArticleResponse.class);   // 응답 데이터를 ArticleResponse 형태로 역직렬화해서 보여주겠다.
    }

    @Test
    void readTest() {
        ArticleResponse response = read(168655294295502848L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId){
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(168655294295502848L);
        ArticleResponse response = read(168655294295502848L);
        System.out.println("response = " + response);
    }

    void update(Long articleId){
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hi 2", "h2 content2"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 168655294295502848L)
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=50000")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()){
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }

}
