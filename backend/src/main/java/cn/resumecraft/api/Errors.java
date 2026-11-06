package cn.resumecraft.api;

import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Errors {
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  ResponseEntity<Api<Void>> missing(Exception e) {
    return ResponseEntity.status(404).body(new Api<>(404, "接口或资源不存在", null));
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  ResponseEntity<Api<Void>> method(Exception e) {
    return ResponseEntity.status(405).body(new Api<>(405, "请求方法不支持", null));
  }

  @ExceptionHandler(BizException.class)
  ResponseEntity<Api<Void>> business(BizException e) {
    return ResponseEntity.status(e.status).body(new Api<>(e.status, e.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Api<Void>> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            new Api<>(400, e.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), null));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<Api<Void>> malformed(Exception e) {
    return ResponseEntity.badRequest().body(new Api<>(400, "请求格式不正确", null));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  ResponseEntity<Api<Void>> duplicate(DuplicateKeyException e) {
    return ResponseEntity.status(409).body(new Api<>(409, "记录已存在，请刷新后重试", null));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Api<Void>> unknown(Exception e) {
    LoggerFactory.getLogger(Errors.class).error("Unhandled API error", e);
    return ResponseEntity.internalServerError().body(new Api<>(500, "服务暂时不可用，请稍后重试", null));
  }
}
