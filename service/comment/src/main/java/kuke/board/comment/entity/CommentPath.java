package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5;      // depth 최대 크기 현재는 5 사이즈 가정
    private static final int MAX_DEPTH = 5;             // 최대 depth 현재는 5 뎁스로 가정

    // MIN_CHUNK = "00000", MAX_CHUNK = "zzzzz"
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    // commentPath 생성 메서드
    public static CommentPath create(String path){
        if(isDepthOverflowed(path)){
            // MAX_DEPTH 넘어가면 5 DEPTH, 에러
            throw new IllegalStateException("depth overflowed");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    // MAX_DEPTH 초과 햇는지 판단 메서드
    private static boolean isDepthOverflowed(String path) {
        return calDepth(path) > DEPTH_CHUNK_SIZE;
    }

    // 댓글의 depth 계산 메서드
    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    // 댓글의 depth 가져오기 메서드
    public int getDepth() {
        return calDepth(path);
    }

    // 댓글이 루트 댓글인지 판단 메서드
    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    // 부모 댓글 path 가져오기 메서드 - root 댓글 가져오는게 아님
    // 00000 00000 00000 의 부모댓글을 가져온다면
    // 00000 00000 이란 말
    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    // 자식댓글 path 생성 메서드
    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if(descendantsTopPath == null){
            // 자식댓글 중에 자식댓글을 가지고 있는 댓글이 없을때
            // 즉, 부모를 기준으로 2 depth 밑에 댓글이 존재하지 않을때
            return CommentPath.create(path + MIN_CHUNK);    // path + 00000 으로 자식 path 설정 - 최초 자식 댓글
        }

        // 부모를 기준으로 2depth 밑에 댓글이 존재할때 - descendantsTopPath 존재할 때
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);   // 해당 부모댓글 자식 댓글중의 최하위 자식댓글 path
        return CommentPath.create(increase(childrenTopPath));               // 최상위 자식댓글 path + 1 로 create
    }

    // 연결된 가장 최하위 자식 전체 댓글 중에 해당 댓글의 부모 댓글 path
    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    // 새로운 자식댓글을 달때 자리값 증가 메서드
    private String increase(String path) {
        // 마지막 자리값 5개
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverflowed(lastChunk)) {
            // 마지막 자리값이 zzzzz 로 우리가 원하는 최대 자리값을 넘지않앗는지 체크
            throw new IllegalStateException("chunk overflowed");
        }

        // 전체 62진수 길이 -> 62
        int charsetLength = CHARSET.length();

        int value = 0;

        // String 변수 -> 10진수 변경 -> lastChunk 만 변경
        for (char ch : lastChunk.toCharArray()) {
            // value * charsetLength 62 진수의 자릿수 올림
            // CHARSET.indexOf(ch) -> 62진수에서 해당 문자의 값 0이면 0 z면 61
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        // 변경된 10진수 +1 -> 새로운 대댓글 이니까
        value = value + 1;


        String result = "";

        // 10진수 다시 String 변수 형으로 변경
        for (int i=0; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        // 마지막 뎁스 변수 새로운 +1 한 String 으로 지정
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverflowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }
}
