package kuke.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class ArticleCreatedTimeRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::article::${articleId}::created-time
    private static final String KEY_FORMAT = "hot-article::article::%s::created-time";

    /**
     * 생성시간까지 레디스로 관리하는 이유
     * 인기글의 경우 당일 생성된 게시글에 대해서만 적용인데 레디스에서 생성시점을 관리하면
     * 굳이 DB 를 한번 더 찔러보지 않고도 인기글 서비스 자체에서 관리가 가능하기 때문에
     */
    public void createOrUpdate(Long articleId, LocalDateTime createdAt, Duration ttl) {
        redisTemplate.opsForValue().set(
                generateKey(articleId),
                String.valueOf(createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()),
                ttl
        );
    }

    public LocalDateTime read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));

        if(result == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.parseLong(result)), ZoneOffset.UTC
        );
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
