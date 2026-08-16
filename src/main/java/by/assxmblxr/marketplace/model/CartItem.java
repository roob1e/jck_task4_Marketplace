package by.assxmblxr.marketplace.model;

/**
 * Domain entity mapped to a row in the {@code cart_items} table.
 * Represents one product line in a {@link User}'s shopping cart, identified by the
 * composite key ({@link #userId()}, {@link #productId()}) rather than a surrogate id:
 * a user can only have one cart row per product, enforced by the table's primary key.
 *
 * @param userId    the {@link User} this cart line belongs to
 * @param productId the {@link Product} added to the cart
 * @param quantity  how many units of the product are in the cart
 */
public record CartItem(
        Long userId,
        Long productId,
        int quantity
) {
}
