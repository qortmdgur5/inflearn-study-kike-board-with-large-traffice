package kuke.board.like.service;

import kuke.board.common.snowflake.Snowflake;
import kuke.board.like.entity.ArticleLike;
import kuke.board.like.entity.ArticleLikeCount;
import kuke.board.like.repository.ArticleLikeCountRepository;
import kuke.board.like.repository.ArticleLikeRepository;
import kuke.board.like.service.reponse.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository  articleLikeCountRepository;

    // 해당 게시물에 해당 유저가 좋아요를 했는지 여부 읽어오기
    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElse(null);
    }

    // 좋아요 서비스 메서드
    /**
     * 바로 update
     */
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        // 좋아요 중간 매핑 테이블 실제 로우 데이터 삽입
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        // 좋아요 카운트 수 테이블 증가 처리
        int result = articleLikeCountRepository.increase(articleId);
        if(result == 0) {
            // 최초 요청 시 update 할 로우 데이터가 존재하지 않으므로
            articleLikeCountRepository.save(
                    // 좋아요 카운트 로우 데이터 초기화 최초 생성
                    // 트래픽이 몰릴때 유실될 수도 있으므로 게시글 생성시점에 미리 0L 카운트로 초기화 시켜두는 것이 좋으나 여기서 그렇게까진 하지않음. 개념적으로만 알자.
                    ArticleLikeCount.init(articleId, 1L)
            );
        }
    }

    // 좋아요 취소 서비스 메서드
    @Transactional
    public void unlikePessimisticLock1(Long  articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike ->{
                    articleLikeRepository.delete(articleLike);
                    articleLikeCountRepository.decrease(articleId);
                });
    }

    // 좋아요 서비스 메서드
    /**
     * select ... for update + update
     */
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        // 비관적 락 가져오고 없으면 좋아요 카운팅 0L 로 초기값 셋팅 및 좋아요 1 증가
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    // 좋아요 취소 서비스 메서드
    @Transactional
    public void unlikePessimisticLock2(Long  articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    // 좋아요 서비스 메서드 - 낙관적 락
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    // 좋아요 취소 서비스 메서드
    @Transactional
    public void unlikeOptimisticLock(Long  articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                        articleLikeRepository.delete(articleLike);
                        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                        articleLikeCount.decrease();
                });
    }

    // 좋아요 수 가져오기 서비스
    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
