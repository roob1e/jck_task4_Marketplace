package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.CartItemDao;
import by.assxmblxr.marketplace.model.CartItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartItemDaoTest extends AbstractDaoTest {
  private final CartItemDao cartItemDao = new CartItemDaoImpl();
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
    executeUpdate("INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)",
            userId, productId, 2);
  }

  @Test
  void testSave() {
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES ((SELECT seller_id FROM products WHERE id = ?), (SELECT category_id FROM products WHERE id = ?), ?, ?, ?, ?) RETURNING id",
            productId, productId, "Board", "M3", BigDecimal.valueOf(120), 5);
    CartItem cartItem = new CartItem(userId, otherProductId, 1);

    CartItem actual = cartItemDao.save(cartItem);

    assertAll(
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(otherProductId, actual.productId()),
            () -> assertEquals(1, actual.quantity())
    );
  }

  @Test
  void testRemovePositive() {
    boolean actual = cartItemDao.remove(userId, productId);

    assertTrue(actual);
    assertTrue(cartItemDao.find(userId, productId).isEmpty());
  }

  @Test
  void testRemoveNegative() {
    boolean actual = cartItemDao.remove(userId, Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testClearPositive() {
    int actual = cartItemDao.clear(userId);

    assertEquals(1, actual);
    assertTrue(cartItemDao.findAllByUser(userId).isEmpty());
  }

  @Test
  void testClearNegative() {
    int actual = cartItemDao.clear(Long.MAX_VALUE);

    assertEquals(0, actual);
  }

  @Test
  void testUpdateQuantityPositive() {
    CartItem cartItem = new CartItem(userId, productId, 5);

    boolean actual = cartItemDao.updateQuantity(cartItem);
    int actualQuantity = cartItemDao.find(userId, productId).orElseGet(Assertions::fail).quantity();

    assertTrue(actual);
    assertEquals(5, actualQuantity);
  }

  @Test
  void testUpdateQuantityNegative() {
    CartItem cartItem = new CartItem(userId, Long.MAX_VALUE, 5);

    boolean actual = cartItemDao.updateQuantity(cartItem);

    assertFalse(actual);
  }

  @Test
  void testFindAllByUser() {
    List<CartItem> actual = cartItemDao.findAllByUser(userId);

    assertEquals(1, actual.size());
    assertEquals(productId, actual.getFirst().productId());
  }

  @Test
  void testFindAllByUserExcludesOtherUsers() {
    Long otherUserId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES" +
                    " ((SELECT role_id FROM users WHERE id = ?), ?, ?) RETURNING id",
            userId, "buyer2", "hash");

    List<CartItem> actual = cartItemDao.findAllByUser(otherUserId);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindPositive() {
    CartItem actual = cartItemDao.find(userId, productId).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(productId, actual.productId()),
            () -> assertEquals(2, actual.quantity())
    );
  }

  @Test
  void testFindNegative() {
    Optional<CartItem> actual = cartItemDao.find(userId, Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }
}
