package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.OrderDao;
import by.assxmblxr.marketplace.model.Order;
import by.assxmblxr.marketplace.model.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDaoTest extends AbstractDaoTest {
  private final OrderDao orderDao = new OrderDaoImpl();
  private Long buyerId;
  private Long roleId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE roles, users, orders RESTART IDENTITY CASCADE");
    roleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    buyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            roleId, "buyer1", "hash");
  }

  @Test
  void testSave() {
    Order order = new Order(null, buyerId, OrderStatus.NEW, null);
    Order savedOrder = orderDao.save(order);

    assertAll(
            () -> assertEquals(1L, savedOrder.id()),
            () -> assertEquals(buyerId, savedOrder.buyerId()),
            () -> assertEquals(OrderStatus.NEW, savedOrder.status()),
            () -> assertNotNull(savedOrder.createdAt())
    );
  }

  @Test
  void updateStatusPositive() {
    OrderStatus expectedStatus = OrderStatus.CANCELLED;

    Order order = new Order(null, buyerId, OrderStatus.NEW, null);
    Long orderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            order.buyerId(), "NEW", OffsetDateTime.now());
    boolean actual = orderDao.updateStatus(orderId, OrderStatus.CANCELLED);
    OrderStatus actualStatus = orderDao.findById(orderId).orElseGet(Assertions::fail).status();

    assertAll(
            () -> assertTrue(actual),
            () -> assertEquals(expectedStatus, actualStatus)
    );
  }

  @Test
  void updateStatusNegative() {
    boolean actual = orderDao.updateStatus(Long.MAX_VALUE, OrderStatus.CANCELLED);

    assertFalse(actual);
  }

  @Test
  void findByIdPositive() {
    OrderStatus expectedStatus = OrderStatus.NEW;

    Order order = new Order(null, buyerId, OrderStatus.NEW, null);
    Long orderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            order.buyerId(), "NEW", OffsetDateTime.now());

    Order actual = orderDao.findById(orderId).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(orderId, actual.id()),
            () -> assertEquals(buyerId, actual.buyerId()),
            () -> assertEquals(expectedStatus, actual.status()),
            () -> assertNotNull(actual.createdAt())
    );
  }

  @Test
  void findByIdNegative() {
    Optional<Order> actual = orderDao.findById(Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }

  @Test
  void findAllByBuyerPositive() {
    Long secondBuyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            roleId, "buyer2", "hash");
    OffsetDateTime[] timestamps = {OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(2)};
    Long[] ids = new Long[3];
    for (int i = 0; i < 3; i++) {
      ids[i] = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
              buyerId, "NEW", timestamps[i]);
    }
    insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            secondBuyerId, "NEW", OffsetDateTime.now());
    Order[] expected = new Order[3];
    for (int i = 0; i < expected.length; i++) {
      expected[i] = new Order(ids[i], buyerId, OrderStatus.NEW, timestamps[i]);
    }

    List<Order> actual = orderDao.findAllByBuyer(buyerId, 1, 10);

    assertArrayEquals(expected, actual.toArray());
  }

  @Test
  void findAllByBuyerNegative() {
    List<Order> actual = orderDao.findAllByBuyer(Long.MAX_VALUE, 1, 10);
    assertTrue(actual.isEmpty());
  }
}