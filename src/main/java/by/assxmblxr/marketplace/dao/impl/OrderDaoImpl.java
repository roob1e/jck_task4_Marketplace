package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.OrderDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.Order;
import by.assxmblxr.marketplace.model.OrderStatus;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDaoImpl implements OrderDao {
  private final ConnectionPool pool;

  public OrderDaoImpl() {
    pool = ConnectionPool.getInstance();
  }

  @Override
  public Order save(Order order) {
    String sql = """
            INSERT INTO orders (buyer_id, status, created_at)
            VALUES (?, ?, ?);
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, order.buyerId());
      ps.setString(2, order.status().name());
      ps.setObject(3, OffsetDateTime.now());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          Long id = rs.getLong("id");
          OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
          order = new Order(id, order.buyerId(), order.status(), createdAt);
          return order;
        } else {
          throw new DaoException("Failed to create an order");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean updateStatus(Long id, OrderStatus status) {
    String sql = """
            UPDATE orders
            SET status = ?
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status.name());
      ps.setLong(2, id);
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Order> findById(Long id) {
    String sql = """
            SELECT * FROM orders
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapFromResultSet(rs));
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Order> findAllByBuyer(Long buyerId, int page, int pageSize) {
    String sql = """
            SELECT * FROM orders
            WHERE buyer_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, buyerId);
      ps.setInt(2, pageSize);
      int offset = (page - 1) * pageSize;
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<Order> orders = new ArrayList<>();
        while (rs.next()) {
          Order order = mapFromResultSet(rs);
          orders.add(order);
        }
        return orders;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private Order mapFromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong("id");
    Long buyerId = rs.getLong("buyer_id");
    OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
    OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
    return new Order(id, buyerId, status, createdAt);
  }
}