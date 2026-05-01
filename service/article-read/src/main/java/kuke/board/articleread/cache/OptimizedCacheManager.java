package kuke.board.articleread.cache;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String DELIMITER = "::";

    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {

        String key = generateKey(type, args);
        String cachedData = redisTemplate.opsForValue().get(key);

        // 1. 캐시 미스 (최초 요청이거나 물리적 TTL 만료)
        if (cachedData == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);

        // 2. 직렬화 에러 (캐시 데이터 오염)
        if (optimizedCache == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 3. 캐시 히트 (논리적 TTL 이내) - 가장 행복한 경로(Happy Path)
        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        // 4. 캐시 갱신 (논리적 TTL 만료) - 스탬피드 방어를 위한 락 획득 시도
        if (!optimizedCacheLockProvider.lock(key)) {
            // 락을 획득하지 못했다면, 다른 스레드가 이미 갱신 중이므로
            // 기다리지 않고 그냥 조금 지난 과거 데이터를 즉시 반환하여 응답 속도 유지
            return optimizedCache.parseData(returnType);
        }

        // 5. 락 획득 성공
        try {
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            optimizedCacheLockProvider.unlock(key); // 예외가 터져도 무조건 락은 해제
        }
    }

    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get();

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());

        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizedCache),
                        optimizedCacheTTL.getPhysicalTTL()
                );

        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args).
                        map(String::valueOf).
                        collect(Collectors.joining(DELIMITER));

    }
}
