package by.assxmblxr.marketplace.model;

import java.math.BigDecimal;

/**
 * Domain entity mapped to a row in the {@code products} table.
 * Represents a single listing offered for sale by a seller.
 *
 * @param id          the primary key, {@code null} for an entity not yet persisted
 * @param sellerId    the id of the {@link User} selling this product
 * @param categoryId  the id of the {@link Category} this product belongs to
 * @param name        the product's display name
 * @param description the product's description
 * @param price       the unit price
 * @param left        the quantity currently in stock
 * @param isDeleted   whether the listing has been deactivated; {@code null} until persisted
 * @param rating      the average review rating, {@code null} if the product has no reviews yet
 */
public record Product(
        Long id,
        Long sellerId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int left,
        Boolean isDeleted,
        BigDecimal rating
) {
}