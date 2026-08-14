package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.ReviewDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.Review;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReviewDaoImpl implements ReviewDao {
  private final ConnectionPool pool;

  public ReviewDaoImpl() {
    pool = ConnectionPool.getInstance();
  }

  @Override
  public Review save(Review review) {
    String sql = """
            INSERT INTO reviews (order_item_id, user_id, product_id, rating, created_at, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, review.orderItemId());
      ps.setLong(2, review.userId());
      ps.setLong(3, review.productId());
      ps.setInt(4, review.rating());
      ps.setObject(5, OffsetDateTime.now());
      ps.setString(6, review.description());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          long id = rs.getLong("id");
          OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
          review = new Review(id, review.orderItemId(), review.userId(), review.productId(),
                  review .rating(), createdAt, review.description());
          return review;
        } else {
          throw new DaoException("Failed to create a review");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Review> update(Review review) {
    Objects.requireNonNull(review.id(), "Cannot update a review without an id");
    String sql = """
            UPDATE reviews
            SET rating = ?, description = ?
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, review.rating());
      ps.setString(2, review.description());
      ps.setLong(3, review.id());
      int rows =  ps.executeUpdate();
      if (rows > 0) {
        return Optional.of(review);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean delete(Long id) {
    String sql = """
            DELETE FROM reviews
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      int rows =  ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Review> findAllByUser(Long userId) {
    String sql = """
            SELECT * FROM reviews
            WHERE user_id = ?
            ORDER BY created_at DESC;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        List<Review> reviews = new ArrayList<>();
        while (rs.next()) {
          Review review = mapFromResultSet(rs);
          reviews.add(review);
        }
        return reviews;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Review> findAllByProduct(Long productId, int page, int pageSize) {
    String sql = """
            SELECT * FROM reviews
            WHERE product_id = ?
            ORDER BY rating DESC
            LIMIT ? OFFSET ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setInt(2, pageSize);
      int offset = (page - 1) * pageSize;
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<Review> reviews = new ArrayList<>();
        while (rs.next()) {
          Review review = mapFromResultSet(rs);
          reviews.add(review);
        }
        return reviews;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<BigDecimal> calculateAverageRating(Long productId) {
    String sql = """
            SELECT AVG(rating) as avg_rating FROM reviews
            WHERE product_id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        BigDecimal avgRating = rs.getBigDecimal("avg_rating");
        return Optional.ofNullable(avgRating);
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private Review mapFromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong("id");
    Long orderItemId = rs.getLong("order_item_id");
    Long userId = rs.getLong("user_id");
    Long productId = rs.getLong("product_id");
    int rating = rs.getInt("rating");
    OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
    String description = rs.getString("description");
    return new Review(id, orderItemId, userId, productId, rating, createdAt, description);
  }
}
