package by.assxmblxr.marketplace.model;

import java.math.BigDecimal;

public record Product(
        Long id,
        Long sellerId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int left,
        boolean isDeleted,
        BigDecimal rating
) {
}
