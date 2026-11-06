package cn.resumecraft.dto;

public record PaymentCallback(
    String orderNo, Integer amount, String transactionId, String status, long timestamp) {}
