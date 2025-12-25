package io.github.yuyeol.youtube_shortener.exception_handling;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BusinessException(ErrorCode errorCode) {
      super(errorCode.getMessage());
      this.httpStatus = errorCode.getStatus();
    }

  @Override
  public String toString() {
    return "BusinessException [httpStatus=" + httpStatus + ", message=" + getMessage() + "]";
  }
}
