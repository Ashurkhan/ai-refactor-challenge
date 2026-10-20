package kg.example.legacy.dto;

import java.math.BigDecimal;

public record OrderTotalResponse(Long orderId, BigDecimal total) {
}
