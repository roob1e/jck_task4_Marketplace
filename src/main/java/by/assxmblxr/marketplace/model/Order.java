package by.assxmblxr.marketplace.model;

import java.time.OffsetDateTime;

/**
 * Domain entity mapped to a row in the {@code orders} table.
 * Represents a single purchase order placed by a buyer, grouping one or more
 * {@link OrderItem}s.
 *
 * @param id        the primary key, {@code null} for an entity not yet persisted
 * @param buyerId   the id of the {@link User} who placed the order
 * @param status    the current lifecycle status of the order
 * @param createdAt the timestamp the order was placed, assigned by the database on insert
 */
public record Order(
        Long id,
        Long buyerId,
        OrderStatus status,
        OffsetDateTime createdAt
) {
}