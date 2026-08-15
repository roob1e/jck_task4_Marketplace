package by.assxmblxr.marketplace.model;

import java.time.OffsetDateTime;

/**
 * Domain entity mapped to a row in the {@code reviews} table.
 * Represents a buyer's rating and comment for a product they purchased,
 * tied to the specific {@link OrderItem} that establishes they actually bought it.
 * A given user may leave at most one review per product.
 *
 * @param id          the primary key, {@code null} for an entity not yet persisted
 * @param orderItemId the id of the {@link OrderItem} that proves the reviewer purchased this product
 * @param userId      the id of the {@link User} who wrote the review
 * @param productId   the id of the {@link Product} being reviewed
 * @param rating      the star rating, from 1 to 5 inclusive
 * @param createdAt   the timestamp the review was submitted, assigned by the database on insert
 * @param description the review's free-text comment, may be {@code null}
 */
public record Review(
        Long id,
        Long orderItemId,
        Long userId,
        Long productId,
        int rating,
        OffsetDateTime createdAt,
        String description
) {}