package cn.resumecraft.service;

import cn.resumecraft.mapper.PaymentLogMapper;
import cn.resumecraft.model.Order;
import cn.resumecraft.model.PaymentLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PaymentLogService {
  private final PaymentLogMapper paymentLogMapper;
  private final ObjectMapper objectMapper;

  public PaymentLogService(PaymentLogMapper paymentLogMapper, ObjectMapper objectMapper) {
    this.paymentLogMapper = paymentLogMapper;
    this.objectMapper = objectMapper;
  }

  public void record(Order order, Object body, String result) {
    PaymentLog log = new PaymentLog();
    log.orderId = order.id;
    log.channel = order.channel;
    log.result = result;
    log.createdAt = LocalDateTime.now();
    try {
      log.rawBody = objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to serialize payment log", exception);
    }
    paymentLogMapper.insert(log);
  }
}
