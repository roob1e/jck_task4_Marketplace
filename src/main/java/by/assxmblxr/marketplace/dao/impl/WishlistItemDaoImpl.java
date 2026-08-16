package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.WishlistItemDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.WishlistItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WishlistItemDaoImpl implements WishlistItemDao {
  private final ConnectionPool pool;

  public WishlistItemDaoImpl() {
    pool = ConnectionPool.getInstance();
  }

  @Override
  public WishlistItem save(WishlistItem wishlistItem) {
    String sql = """
            INSERT INTO wishlist_items (user_id, product_id)
            VALUES (?, ?);
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, wishlistItem.userId());
      ps.setLong(2,  wishlistItem.productId());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return wishlistItem;
      } else {
        throw new DaoException("Failed to create wishlistItem");
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean remove(Long userId, Long productId) {
    String sql = """
            DELETE FROM wishlist_items
            WHERE user_id = ? AND product_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2,  productId);
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<WishlistItem> findAllByUser(Long userId) {
    String sql = """
            SELECT * FROM wishlist_items
            WHERE user_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        List<WishlistItem> wishlistItems = new ArrayList<>();
        while (rs.next()) {
          wishlistItems.add(mapFromResultSet(rs));
        }
        return wishlistItems;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<WishlistItem> find(Long userId, Long productId) {
    String sql = """
            SELECT * FROM wishlist_items
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

  private WishlistItem mapFromResultSet(ResultSet rs) throws SQLException {
    Long userId = rs.getLong("user_id");
    Long productId = rs.getLong("product_id");
    return new WishlistItem(userId, productId);
  }
}
