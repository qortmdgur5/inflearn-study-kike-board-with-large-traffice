package kuke.board.hotarticle.service;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.hotarticle.repository.ArticleCreatedTimeRepository;
import kuke.board.hotarticle.repository.HotArticleListRepository;
import kuke.board.hotarticle.service.eventhandler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;
    private final HotArticleListRepository hotArticleListRepository;

    private static final long HOT_ARTICLE_COUNT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
        Long articleId = eventHandler.findArticleId(event);
        LocalDateTime createdTime = articleCreatedTimeRepository.read(articleId);

        // 오늘 날짜가 아니라면 그냥 리턴
        if (!isArticleCreatedToday(createdTime)) {
            return;
        }

        // 이벤트 핸들 처리 - 각 리포지토리 카운팅 업데이트를 담당
        eventHandler.handle(event);

        // 인기글 점수 계산
        long score = hotArticleScoreCalculator.calculate(articleId);

        // 인기글 점수 계산 후 레디스 점수 랭킹배열에 add
        hotArticleListRepository.add(
                articleId,
                createdTime,
                score,
                HOT_ARTICLE_COUNT,
                HOT_ARTICLE_TTL
        );
    }

    private boolean isArticleCreatedToday(LocalDateTime createdTime) {
        return createdTime != null && createdTime.toLocalDate().equals(LocalDate.now());
    }
}
