package kuke.board.article.service.response;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ArticlePageResponse {
    private List<ArticleResponse> articles;
    private Long articleCount;

    // "정적 팩토리 메서드" 패턴 - public static Class
    // 불변 객체란?
    // 생성된 이후에는 절대 내부 상태가 바뀌지 않는 객체
    // 즉, setter 없음, 필드는 보통 final 혹은 private
    // 동시성 문제가 적고, 예측 가능한 코드 작성에 유리
    public static ArticlePageResponse of(List<ArticleResponse> articles, Long articleCount) {
        ArticlePageResponse response = new ArticlePageResponse();
        response.articles = articles;
        response.articleCount = articleCount;
        return response;
    }
}
