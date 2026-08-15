package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.OrderItemDao;
import by.assxmblxr.marketplace.model.OrderItem;
import by.assxmblxr.marketplace.model.OrderItemStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemDaoTest extends AbstractDaoTest {
  private final OrderItemDao orderItemDao = new OrderItemDaoImpl();
  private Long buyerRoleId;
  private Long sellerRoleId;
  private Long sellerId;
  private Long categoryId;
  private Long productId;
  private Long orderId;
  private Long orderItemId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles, categories RESTART IDENTITY CASCADE");
    buyerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    sellerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    Long buyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "buyer1", "hash");
    sellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller1", "hash");
    categoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Electronics");
    productId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
    orderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            buyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    orderItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            orderId, productId, 2, BigDecimal.valueOf(335.5), "ACTIVE");
  }

  @Test
  void testSave() {
    OrderItem orderItem = new OrderItem(null, orderId, productId, 1, BigDecimal.valueOf(335.5), OrderItemStatus.ACTIVE);

    OrderItem actual = orderItemDao.save(orderItem);

    assertAll(
            () -> assertNotNull(actual.id()),
            () -> assertEquals(orderId, actual.orderId()),
            () -> assertEquals(productId, actual.productId()),
            () -> assertEquals(1, actual.quantity()),
            () -> assertEquals(0, BigDecimal.valueOf(335.5).compareTo(actual.priceAtPurchase())),
            () -> assertEquals(OrderItemStatus.ACTIVE, actual.status())
    );
  }

  @Test
  void testUpdateStatusPositive() {
    boolean actual = orderItemDao.updateStatus(orderItemId, OrderItemStatus.CANCELLED);
    OrderItemStatus actualStatus = orderItemDao.findById(orderItemId).orElseGet(Assertions::fail).status();

    assertTrue(actual);
    assertEquals(OrderItemStatus.CANCELLED, actualStatus);
  }

  @Test
  void testUpdateStatusNegative() {
    boolean actual = orderItemDao.updateStatus(Long.MAX_VALUE, OrderItemStatus.CANCELLED);

    assertFalse(actual);
  }

  @Test
  void testFindByIdPositive() {
    OrderItem actual = orderItemDao.findById(orderItemId).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(orderItemId, actual.id()),
            () -> assertEquals(orderId, actual.orderId()),
            () -> assertEquals(productId, actual.productId()),
            () -> assertEquals(2, actual.quantity()),
            () -> assertEquals(0, BigDecimal.valueOf(335.5).compareTo(actual.priceAtPurchase())),
            () -> assertEquals(OrderItemStatus.ACTIVE, actual.status())
    );
  }

  @Test
  void testFindByIdNegative() {
    Optional<OrderItem> actual = orderItemDao.findById(Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindAllByOrder() {
    Long secondItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            orderId, productId, 1, BigDecimal.valueOf(335.5), "ACTIVE");

    List<OrderItem> actual = orderItemDao.findAllByOrder(orderId);

    assertEquals(2, actual.size());
    assertEquals(orderItemId, actual.get(0).id());
    assertEquals(secondItemId, actual.get(1).id());
  }

  @Test
  void testFindAllByOrderExcludesOtherOrders() {
    Long otherBuyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "buyer2", "hash");
    Long otherOrderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            otherBuyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            otherOrderId, productId, 1, BigDecimal.valueOf(335.5), "ACTIVE");

    List<OrderItem> actual = orderItemDao.findAllByOrder(orderId);

    assertEquals(1, actual.size());
    assertEquals(orderItemId, actual.getFirst().id());
  }

  @Test
  void testFindAllBySellerPagination() {
    Long secondItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            orderId, productId, 1, BigDecimal.valueOf(335.5), "ACTIVE");

    List<OrderItem> actual1 = orderItemDao.findAllBySeller(sellerId, 1, 1);
    List<OrderItem> actual2 = orderItemDao.findAllBySeller(sellerId, 2, 1);

    assertEquals(1, actual1.size());
    assertEquals(orderItemId, actual1.getFirst().id());
    assertEquals(1, actual2.size());
    assertEquals(secondItemId, actual2.getFirst().id());
  }

  @Test
  void testFindAllBySellerExcludesOtherSellers() {
    Long otherSellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller2", "hash");
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            otherSellerId, categoryId, "Board", "M3", BigDecimal.valueOf(120), 5);
    insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            orderId, otherProductId, 1, BigDecimal.valueOf(120), "ACTIVE");

    List<OrderItem> actual = orderItemDao.findAllBySeller(sellerId, 1, 10);

    assertEquals(1, actual.size());
    assertEquals(orderItemId, actual.getFirst().id());
  }
}
