package kuke.board.common.outboxmessagerelay;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageRelayConstants {
    public static final int SHARD_COUNT = 4; // 임의의 값, 실제 샤드 구성은 안했지만 샤드가 있다는 가정하에 개발이기 떄문
}
