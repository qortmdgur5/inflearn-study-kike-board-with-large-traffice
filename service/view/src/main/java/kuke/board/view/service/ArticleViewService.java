package kuke.board.view.service;

import kuke.board.view.repository.ArticleViewCountBackUpRepository;
import kuke.board.view.repository.ArticleViewCountRepository;
import kuke.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;
    private static final int BACK_UP_BATCH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {
        if(!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
            return articleViewCountRepository.read(articleId);
        }
        Long count = articleViewCountRepository.increase(articleId);
        if(count % BACK_UP_BATCH_SIZE == 0) {
            // 레디스에 저장된 순간 조회 수가 100으로 나눠서 0 즉, 100 개씩 올라간 사이즈일때 실제 백업 업데이트
            articleViewCountBackUpRepository.updateViewCount(articleId, count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}
