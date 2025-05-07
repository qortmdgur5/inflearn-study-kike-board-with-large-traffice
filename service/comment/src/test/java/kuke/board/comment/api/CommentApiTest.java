package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class CommentApiTest {

    // 9001 포트의 Comment Application 클라이언트 요청 생성
    RestClient restClient = RestClient.create("http://localhost:9001");

    // Comment 생성 테스트
    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "mycontent1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "mycontent2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "mycontent3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));
    }


    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 178425050763489280L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        //    commentId=178425050763489280
        //      commentId=178425051447160832
        //      commentId=178425051518464000
        restClient.delete()
                .uri("/v1/comments/{commentId}", 178425051518464000L)
                .retrieve();
    }

    // Comment 생성 요청 Entity
    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
