package kuke.board.like.repository;

import kuke.board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike,Long> {
    // 게시물 & 좋아요 한 유저로 찾기
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
