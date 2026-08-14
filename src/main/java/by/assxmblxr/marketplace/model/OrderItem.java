package by.assxmblxr.marketplace.model;

import java.math.BigDecimal;

public record OrderItem(
        Long id,
        Long orderId,
        Long productId,
        int quantity,
        BigDecimal priceAtPurchase,
        OrderItemStatus status
) {
}