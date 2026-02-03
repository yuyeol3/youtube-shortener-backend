package io.github.yuyeol.youtube_shortener.exception_handling;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    InvalidYoutubeURL("주소가 올바르지 않습니다.", HttpStatus.NOT_FOUND),
    CouldNotParseHeatMap("시청 데이터를 파싱할 수 없습니다.", HttpStatus.NOT_FOUND),
    CouldNotFetchHeatMapData("시청 데이터를 가져올 수 없습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus status;


    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
