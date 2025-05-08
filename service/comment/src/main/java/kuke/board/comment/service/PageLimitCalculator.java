package kuke.board.comment.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// final class - 유틸 클래스 패턴
// 다른 클래스 상속 불가
// 바꾸지 말고 일반 유틸 클래스 처럼 가져다 만 써라 수정 하면 큰일 난다 는 의미
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {

    // movablePage - 보여지는 움직일 수 있는 페이지 수에 따른 다음 영역의 데이터 셋 시작 계산 함수
    // ex) movablePage = 10, page = 5, pageSize = 30
    // 보여지는 페이지는 1~10
    // 현재 페이지 5 패이지
    // 페이지 당 데이터 30개
    // 다음 영역 11~20 페이지의 데이터 시작은 301
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount){
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
