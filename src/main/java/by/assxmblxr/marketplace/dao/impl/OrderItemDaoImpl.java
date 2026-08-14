package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.OrderItemDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.OrderItem;
import by.assxmblxr.marketplace.model.OrderItemStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderItemDaoImpl implements OrderItemDao {
  private final ConnectionPool pool;

  public OrderItemDaoImpl() {
    this.pool = ConnectionPool.getInstance();
  }

  @Override
  public OrderItem save(OrderItem orderItem) {
    String sql = """
            INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)
            VALUES (?, ?, ?, ?, ?)
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, orderItem.orderId());
      ps.setLong(2, orderItem.productId());
      ps.setInt(3, orderItem.quantity());
      ps.setBigDecimal(4, orderItem.priceAtPurchase());
      ps.setString(5, orderItem.status().name());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          Long id = rs.getLong("id");
          orderItem = new OrderItem(id, orderItem.orderId(), orderItem.productId(), orderItem.quantity(),
                  orderItem.priceAtPurchase(), orderItem.status());
          return orderItem;
        } else {
          throw new DaoException("Failed to create an order item");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean updateStatus(Long id, OrderItemStatus status) {
    String sql = """
            UPDATE order_items
            SET status = ?
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status.name());
      ps.setLong(2, id);
      int rows =  ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<OrderItem> findById(Long id) {
    String sql = """
            SELECT * FROM order_items
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
  public List<OrderItem> findAllByOrder(Long orderId) {
    String sql = """
            SELECT * FROM order_items
            WHERE order_id = ?
            ORDER BY id;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, orderId);
      try (ResultSet rs = ps.executeQuery()) {
        List<OrderItem> orderItems = new ArrayList<>();
        while (rs.next()) {
          OrderItem orderItem = mapFromResultSet(rs);
          orderItems.add(orderItem);
        }
        return orderItems;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<OrderItem> findAllBySeller(Long sellerId, int page, int pageSize) {
    String sql = """
            SELECT oi.* FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            WHERE p.seller_id = ?
            ORDER BY oi.id
            LIMIT ? OFFSET ?
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, sellerId);
      ps.setInt(2, pageSize);
      int offset = (page - 1) * pageSize;
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<OrderItem> orderItems = new ArrayList<>();
        while (rs.next()) {
          OrderItem orderItem = mapFromResultSet(rs);
          orderItems.add(orderItem);
        }
        return orderItems;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private OrderItem mapFromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong("id");
    Long orderId = rs.getLong("order_id");
    Long productId = rs.getLong("product_id");
    int quantity = rs.getInt("quantity");
    BigDecimal priceAtPurchase = rs.getBigDecimal("price_at_purchase");
    OrderItemStatus status = OrderItemStatus.valueOf(rs.getString("status"));
    return new OrderItem(id, orderId, productId, quantity, priceAtPurchase, status);
  }
}
