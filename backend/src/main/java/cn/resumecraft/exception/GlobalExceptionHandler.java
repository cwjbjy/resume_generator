package cn.resumecraft.exception;

import cn.resumecraft.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ApiResponse<Void>> missing(Exception e) {
    return ResponseEntity.status(404).body(new ApiResponse<>(404, "接口或资源不存在", null));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiResponse<Void>> method(Exception e) {
    return ResponseEntity.status(405).body(new ApiResponse<>(405, "请求方法不支持", null));
  }

  @ExceptionHandler(BizException.class)
  ResponseEntity<ApiResponse<Void>> business(BizException e) {
    return ResponseEntity.status(e.status).body(new ApiResponse<>(e.status, e.getMessage(), null));
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ApiResponse<Void>> authentication(BadCredentialsException e) {
    return ResponseEntity.status(401).body(new ApiResponse<>(401, "用户名或密码错误", null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            new ApiResponse<>(
                400, e.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), null));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ApiResponse<Void>> malformed(Exception e) {
    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "请求格式不正确", null));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  ResponseEntity<ApiResponse<Void>> duplicate(DuplicateKeyException e) {
    return ResponseEntity.status(409).body(new ApiResponse<>(409, "记录已存在，请刷新后重试", null));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> unknown(Exception e) {
    log.error("Unhandled API error", e);
    return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "服务暂时不可用，请稍后重试", null));
  }
}
