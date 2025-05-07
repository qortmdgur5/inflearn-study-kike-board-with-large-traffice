package kuke.board.comment.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuke.board.comment.entity.Comment;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class DataInitializer {

    // JPA의 EntityManager를 주입받아 엔티티의 영속성 관리(저장, 조회 등)를 처리합니다.
    @PersistenceContext
    EntityManager entityManager;

    // 트랜잭션을 명시적으로 제어하기 위해 Spring의 TransactionTemplate을 자동 주입
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();
    // CountDownLatch는 다중 스레드가 완료될 때까지 메인 스레드가 기다릴 수 있도록 도와주는 동기화 도구입니다.
    // EXECUTE_COUNT 만큼의 스레드 작업이 완료될 때까지 기다립니다.
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    // BULK_INSERT_SIZE: 한 번의 트랜잭션에서 삽입할 Article 수입니다.
    // EXECUTE_COUNT: 병렬로 실행할 스레드 수입니다 (전체적으로 6000 * 2000 = 1,200만 건 삽입 시도).
    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 6000;

    // InterruptedException은 latch.await() 호출 시 예외 발생 가능성 때문에 선언됩니다.
    @Test
    void initialize() throws InterruptedException {
        // 최대 10개의 스레드를 병렬로 실행하기 위한 스레드 풀을 생성합니다.
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // EXECUTE_COUNT만큼 반복하면서 각 작업을 별도 스레드에서 실행하도록 제출(submit)합니다.
        // insert() 메서드를 호출해서 DB에 데이터 삽입.
        // 삽입 후 latch.countDown()으로 카운트를 감소시켜 완료를 알림.
        // 현재 남은 작업 수를 출력하여 진행 상황을 확인할 수 있습니다.
        for(int i = 0; i < EXECUTE_COUNT; i++){
            executorService.submit(() -> {
                insert();
                latch.countDown();
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        // 모든 스레드 작업이 끝날 때까지 대기합니다.
        latch.await();
        // 작업이 모두 끝난 후 스레드 풀을 종료합니다.
        executorService.shutdown();
    }

    void insert() {
        // 트랜잭션 범위를 지정해주며, 트랜잭션 내부에서 롤백이나 커밋을 처리합니다.
        // executeWithoutResult()는 반환값이 없는 트랜잭션 수행입니다.
        transactionTemplate.executeWithoutResult(status -> {
            Comment prev = null;
            for(int i = 0; i < BULK_INSERT_SIZE; i++){
                Comment comment = Comment.create(
                        snowflake.nextId(),
                        "content",
                        i % 2 == 0 ? null : prev.getCommentId(), // parentCommentId = i 가 짝수일때 null 홀수일때 이전 commentId 이러면 반반 2뎁스 되겟지
                        1L,
                        1L
                );
                prev = comment;

                entityManager.persist(comment);
            }
        });

    }

}
