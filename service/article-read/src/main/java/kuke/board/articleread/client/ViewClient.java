package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import kuke.board.articleread.cache.OptimizedCachable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewClient {
    private RestClient restClient;

    @Value("${endpoints.kuke-board-view-service.uri}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

    // article-read 서비스(현재 코드 서비스) 의 redis 에서 캐싱된 데이터를 조회한다,
    // 없으면 view count 서비스 클라이언트를 호출해서 viewCount API 를 호출한다.
    // 이후 로직은 해당 서비스에서 할테니 우리는 몰라도 된다. 이게 설계의 원칙.

//    @Cacheable(key = "#articleId", value = "articleViewCount") - 기존 일반 캐셔블 어노테아션
    @OptimizedCachable(type = "articleViewCount", ttlSeconds = 1)
    public long count(Long articleId) {
        log.info("[ViewCline.count] articleId = {}",  articleId);
        try {
            return restClient.get()
                    .uri("/v1/article-views/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ViewClient.count] articleId = {}", articleId);
            return 0;
        }
    }
}
