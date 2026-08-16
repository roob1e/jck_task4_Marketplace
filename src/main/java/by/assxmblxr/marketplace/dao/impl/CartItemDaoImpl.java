package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.CartItemDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.CartItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartItemDaoImpl implements CartItemDao {
  private final ConnectionPool pool;

  public CartItemDaoImpl() {
    pool = ConnectionPool.getInstance();
  }

  @Override
  public CartItem save(CartItem cartItem) {
    String sql = """
            INSERT INTO cart_items (user_id, product_id, quantity)
            VALUES (?, ?, ?)
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, cartItem.userId());
      ps.setLong(2, cartItem.productId());
      ps.setInt(3, cartItem.quantity());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return cartItem;
      } else {
        throw new DaoException("Failed to create cartItem");
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean remove(Long userId, Long productId) {
    String sql = """
            DELETE FROM cart_items
            WHERE user_id = ? AND product_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, productId);
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public int clear(Long userId) {
    String sql = """
            DELETE FROM cart_items
            WHERE user_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean updateQuantity(CartItem cartItem) {
    String sql = """
            UPDATE cart_items
            SET quantity = ?
            WHERE user_id = ? AND product_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, cartItem.quantity());
      ps.setLong(2, cartItem.userId());
      ps.setLong(3, cartItem.productId());
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<CartItem> findAllByUser(Long userId) {
    String sql = """
            SELECT * FROM cart_items
            WHERE user_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        List<CartItem> cartItems = new ArrayList<>();
        while (rs.next()) {
          cartItems.add(mapFromResultSet(rs));
        }
        return cartItems;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<CartItem> find(Long userId, Long productId) {
    String sql = """
            SELECT * FROM cart_items
            WHERE user_id = ? AND product_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, productId);
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

  private CartItem mapFromResultSet(ResultSet rs) throws SQLException {
    Long userId = rs.getLong("user_id");
    Long productId = rs.getLong("product_id");
    int quantity = rs.getInt("quantity");
    return new CartItem(userId, productId, quantity);
  }
}
