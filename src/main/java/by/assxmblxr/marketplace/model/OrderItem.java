package by.assxmblxr.marketplace.model;

import java.math.BigDecimal;

/**
 * Domain entity mapped to a row in the {@code order_items} table.
 * Represents a single product line within an {@link Order}.
 *
 * @param id              the primary key, {@code null} for an entity not yet persisted
 * @param orderId         the id of the {@link Order} this item belongs to
 * @param productId       the id of the {@link Product} purchased
 * @param quantity        the number of units purchased
 * @param priceAtPurchase the unit price at the time of purchase, snapshotted so later
 *                        price changes on the product don't retroactively affect past orders
 * @param status          whether this line item is still active or was cancelled
 */
public record OrderItem(
        Long id,
        Long orderId,
        Long productId,
        int quantity,
        BigDecimal priceAtPurchase,
        OrderItemStatus status
) {
}