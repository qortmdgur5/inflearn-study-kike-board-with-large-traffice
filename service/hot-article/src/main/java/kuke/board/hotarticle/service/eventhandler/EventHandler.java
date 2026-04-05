package kuke.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event); // 이벤트를 받았을때 처리하는 핸들 로직
    boolean supports(Event<T> event); // 이벤트 핸들러 구현체가 지원하는 이벤트인지 확인하는 서포트
    Long findArticleId(Event<T> event); // 이벤트가 어떤 게시글에 해당하는지 확인하는 메서드
}
