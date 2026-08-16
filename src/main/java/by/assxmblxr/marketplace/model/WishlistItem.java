package by.assxmblxr.marketplace.model;

/**
 * Domain entity mapped to a row in the {@code wishlist_items} table.
 * Represents a {@link User}'s interest in a {@link Product}, identified by the
 * composite key ({@link #userId()}, {@link #productId()}) rather than a surrogate id:
 * a user can only wishlist a given product once, enforced by the table's primary key.
 *
 * @param userId    the {@link User} who wishlisted the product
 * @param productId the {@link Product} that was wishlisted
 */
public record WishlistItem(
  Long userId,
  Long productId
) {
}
