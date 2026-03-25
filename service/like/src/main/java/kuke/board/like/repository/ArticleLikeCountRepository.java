package kuke.board.like.repository;

import jakarta.persistence.LockModeType;
import kuke.board.like.entity.ArticleLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {
    // select ... for update 비관적 락 쿼리
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);

    // 좋아요 증가
    @Query(
            value = "update article_like_count set like_count = like_count + 1 where articleId = :articleId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("articleId") Long articleId);

    // 좋아요 감소
    @Query(
            value = "update article_like_count set like_count = like_count - 1 where articleId = :articleId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("articleId") Long articleId);
}
