package by.assxmblxr.marketplace.model;

import java.time.OffsetDateTime;

public record Review(
        Long id,
        Long orderItemId,
        Long userId,
        Long productId,
        int rating,
        OffsetDateTime createdAt,
        String description
) {}