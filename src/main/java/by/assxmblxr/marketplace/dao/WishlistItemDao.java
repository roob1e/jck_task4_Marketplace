package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.WishlistItem;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link WishlistItem} entities.
 */
public interface WishlistItemDao {

  /**
   * Adds a new product to a user's wishlist. Does not check whether an entry for the
   * same ({@link WishlistItem#userId()}, {@link WishlistItem#productId()}) pair already
   * exists; callers are expected to check via {@link #find(Long, Long)} first, since the
   * table's primary key rejects a duplicate pair with a constraint violation.
   *
   * @param wishlistItem the wishlist entry to create
   * @return the persisted wishlist entry, unchanged
   */
  WishlistItem save(WishlistItem wishlistItem);

  /**
   * Removes a product from a user's wishlist.
   *
   * @param userId    the id of the user whose wishlist is being modified
   * @param productId the id of the product to remove
   * @return {@code true} if a matching wishlist entry was found and deleted, {@code false} otherwise
   */
  boolean remove(Long userId, Long productId);

  /**
   * Finds every product on a user's wishlist.
   *
   * @param userId the id of the user whose wishlist to retrieve
   * @return the user's wishlist entries, possibly empty
   */
  List<WishlistItem> findAllByUser(Long userId);

  /**
   * Finds a single wishlist entry by user and product, e.g. to show whether a product
   * is already on the user's wishlist on the product page.
   *
   * @param userId    the id of the user whose wishlist to search
   * @param productId the id of the product to look up
   * @return an {@link Optional} containing the matching wishlist entry,
   *         or {@link Optional#empty()} if the product is not on the user's wishlist
   */
  Optional<WishlistItem> find(Long userId, Long productId);
}
