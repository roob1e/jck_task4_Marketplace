package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.WishlistItemDao;
import by.assxmblxr.marketplace.model.WishlistItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WishlistItemDaoTest extends AbstractDaoTest {
  private final WishlistItemDao wishlistItemDao = new WishlistItemDaoImpl();
  private Long userId;
  private Long productId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles, categories RESTART IDENTITY CASCADE");
    Long buyerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    Long sellerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    userId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "buyer1", "hash");
    Long sellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller1", "hash");
    Long categoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Electronics");
    productId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
    executeUpdate("INSERT INTO wishlist_items (user_id, product_id) VALUES (?, ?)", userId, productId);
  }

  @Test
  void testSave() {
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES ((SELECT seller_id FROM products WHERE id = ?), (SELECT category_id FROM products WHERE id = ?), ?, ?, ?, ?) RETURNING id",
            productId, productId, "Board", "M3", BigDecimal.valueOf(120), 5);
    WishlistItem wishlistItem = new WishlistItem(userId, otherProductId);

    WishlistItem actual = wishlistItemDao.save(wishlistItem);

    assertAll(
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(otherProductId, actual.productId())
    );
  }

  @Test
  void testRemovePositive() {
    boolean actual = wishlistItemDao.remove(userId, productId);

    assertTrue(actual);
    assertTrue(wishlistItemDao.find(userId, productId).isEmpty());
  }

  @Test
  void testRemoveNegative() {
    boolean actual = wishlistItemDao.remove(userId, Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testFindAllByUser() {
    List<WishlistItem> actual = wishlistItemDao.findAllByUser(userId);

    assertEquals(1, actual.size());
    assertEquals(productId, actual.getFirst().productId());
  }

  @Test
  void testFindAllByUserExcludesOtherUsers() {
    Long otherUserId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES" +
                    " ((SELECT role_id FROM users WHERE id = ?), ?, ?) RETURNING id",
            userId, "buyer2", "hash");

    List<WishlistItem> actual = wishlistItemDao.findAllByUser(otherUserId);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindPositive() {
    WishlistItem actual = wishlistItemDao.find(userId, productId).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(productId, actual.productId())
    );
  }

  @Test
  void testFindNegative() {
    Optional<WishlistItem> actual = wishlistItemDao.find(userId, Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }
}
