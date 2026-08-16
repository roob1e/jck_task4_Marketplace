package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.CartItem;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link CartItem} entities.
 */
public interface CartItemDao {

  /**
   * Adds a new line to a cart. Does not check whether a line for the same
   * ({@link CartItem#userId()}, {@link CartItem#productId()}) pair already exists;
   * callers are expected to check via {@link #find(Long, Long)} first, since the
   * table's primary key rejects a duplicate pair with a constraint violation.
   *
   * @param cartItem the cart line to create
   * @return the persisted cart line, unchanged
   */
  CartItem save(CartItem cartItem);

  /**
   * Removes a single product line from a user's cart.
   *
   * @param userId    the id of the user whose cart is being modified
   * @param productId the id of the product to remove
   * @return {@code true} if a matching cart line was found and deleted, {@code false} otherwise
   */
  boolean remove(Long userId, Long productId);

  /**
   * Removes every line from a user's cart.
   *
   * @param userId the id of the user whose cart should be emptied
   * @return the number of cart lines deleted
   */
  int clear(Long userId);

  /**
   * Updates the quantity of an existing cart line, identified by
   * ({@link CartItem#userId()}, {@link CartItem#productId()}).
   *
   * @param cartItem the cart line carrying the new quantity
   * @return {@code true} if a matching cart line was found and updated, {@code false} otherwise
   */
  boolean updateQuantity(CartItem cartItem);

  /**
   * Finds every line in a user's cart.
   *
   * @param userId the id of the user whose cart to retrieve
   * @return the user's cart lines, possibly empty
   */
  List<CartItem> findAllByUser(Long userId);

  /**
   * Finds a single cart line by user and product, e.g. to show whether a product
   * is already in the user's cart on the product page.
   *
   * @param userId    the id of the user whose cart to search
   * @param productId the id of the product to look up
   * @return an {@link Optional} containing the matching cart line,
   *         or {@link Optional#empty()} if the product is not in the user's cart
   */
  Optional<CartItem> find(Long userId, Long productId);
}
