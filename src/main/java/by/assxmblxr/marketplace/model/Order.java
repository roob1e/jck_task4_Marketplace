package by.assxmblxr.marketplace.model;

import java.time.OffsetDateTime;

public record Order(
        Long id,
        Long buyerId,
        OrderStatus status,
        OffsetDateTime createdAt
) {
}
