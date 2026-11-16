package cn.resumecraft.exception;

import cn.resumecraft.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception e, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    String message =
        switch (status.value()) {
          case 400 -> "请求参数或格式不正确";
          case 404 -> "接口或资源不存在";
          case 405 -> "请求方法不支持";
          case 406 -> "不支持请求的响应格式";
          case 413 -> "请求内容过大";
          case 415 -> "不支持请求的媒体类型";
          default -> "请求处理失败";
        };
    if (e instanceof MethodArgumentNotValidException validation) {
      message =
          validation.getBindingResult().getAllErrors().stream()
              .findFirst()
              .map(error -> error.getDefaultMessage())
              .orElse(message);
    }
    return new ResponseEntity<>(new ApiResponse<>(status.value(), message, null), headers, status);
  }

  @ExceptionHandler(BizException.class)
  ResponseEntity<ApiResponse<Void>> business(BizException e) {
    return ResponseEntity.status(e.status).body(new ApiResponse<>(e.status, e.getMessage(), null));
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ApiResponse<Void>> authentication(BadCredentialsException e) {
    return ResponseEntity.status(401).body(new ApiResponse<>(401, "用户名或密码错误", null));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiResponse<Void>> validation(ConstraintViolationException e) {
    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "请求参数不正确", null));
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
