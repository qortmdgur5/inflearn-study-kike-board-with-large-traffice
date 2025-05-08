package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for(CommentResponse comment : response.getComments()){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                // 루트 댓글이 아니라면
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /** 1번 페이지 수행결과
     * comment.getCommentId() = 178431893429067776
     * 	comment.getCommentId() = 178431893508759555
     * comment.getCommentId() = 178431893429067777
     * 	comment.getCommentId() = 178431893508759554
     * comment.getCommentId() = 178431893429067778
     * 	comment.getCommentId() = 178431893508759558
     * comment.getCommentId() = 178431893429067779
     * 	comment.getCommentId() = 178431893508759552
     * comment.getCommentId() = 178431893429067780
     * 	comment.getCommentId() = 178431893508759559
     */
    
    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&limit=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for(CommentResponse comment : response1){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                // 루트 댓글이 아니라면
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&limit=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("secondPage");
        for(CommentResponse comment : response2){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                // 루트 댓글이 아니라면
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
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
