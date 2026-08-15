package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.ProductDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {
  private final ConnectionPool pool;

  public ProductDaoImpl() {
    this.pool = ConnectionPool.getInstance();
  }

  @Override
  public Product save(Product product) {
    String sql = """
            INSERT INTO products (seller_id, category_id, name, description, price, "left")
            VALUES (?, ?, ?, ?, ?, ?);
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, product.sellerId());
      ps.setLong(2, product.categoryId());
      ps.setString(3, product.name());
      ps.setString(4, product.description());
      ps.setBigDecimal(5, product.price());
      ps.setInt(6, product.left());
      ps.execute();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          long id = rs.getLong("id");
          boolean isDeleted = rs.getBoolean("is_deleted");
          product = new Product(id, product.sellerId(), product.categoryId(), product.name(), product.description(),
                  product.price(), product.left(), isDeleted, null);
          return product;
        } else {
          throw new DaoException("Failed to create a product");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Product> update(Product product) {
    Objects.requireNonNull(product.id(), "Cannot update a product without an id");
    String sql = """
            UPDATE products
            SET category_id = ?, name = ?, description = ?, price = ?, "left" = ?
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, product.categoryId());
      ps.setString(2, product.name());
      ps.setString(3, product.description());
      ps.setBigDecimal(4, product.price());
      ps.setInt(5, product.left());
      ps.setLong(6, product.id());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return Optional.of(product);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean deactivate(Long id) {
    String sql = """
            UPDATE products
            SET is_deleted = true
            WHERE id = ? AND is_deleted = false;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      int rows = ps.executeUpdate();
      return rows == 1;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Product> findById(Long id) {
    String sql = """
            SELECT * FROM products WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapFromResultSet(rs));
        } else {
          return Optional.empty();
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Product> findAllActive(int page, int pageSize) {
    String sql = """
            SELECT * FROM products
            WHERE is_deleted = false
            ORDER BY rating DESC NULLS LAST
            LIMIT ? OFFSET ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      int offset = (page - 1) * pageSize;
      ps.setInt(1, pageSize);
      ps.setInt(2, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<Product> products = new ArrayList<>();
        while (rs.next()) {
          Product product = mapFromResultSet(rs);
          products.add(product);
        }
        return products;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Product> findAllBySeller(Long sellerId, int page, int pageSize) {
    String sql = """
            SELECT * FROM products
            WHERE seller_id = ?
            ORDER BY id
            LIMIT ? OFFSET ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      int offset = (page - 1) * pageSize;
      ps.setLong(1, sellerId);
      ps.setInt(2, pageSize);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<Product> products = new ArrayList<>();
        while (rs.next()) {
          Product product = mapFromResultSet(rs);
          products.add(product);
        }
        return products;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public List<Product> findAllByCategory(Long categoryId, int page, int pageSize) {
    String sql = """
            SELECT * FROM products
            WHERE category_id = ?
            ORDER BY rating DESC NULLS LAST
            LIMIT ? OFFSET ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      int offset = (page - 1) * pageSize;
      ps.setLong(1, categoryId);
      ps.setInt(2, pageSize);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        List<Product> products = new ArrayList<>();
        while (rs.next()) {
          Product product = mapFromResultSet(rs);
          products.add(product);
        }
        return products;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public void updateRating(Long productId, BigDecimal rating) {
    String sql = """
            UPDATE products
            SET rating = ?
            WHERE id = ?;
    """;
    try (Connection conn = pool.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setBigDecimal(1, rating);
      ps.setLong(2, productId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private Product mapFromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong("id");
    Long sellerId = rs.getLong("seller_id");
    Long categoryId = rs.getLong("category_id");
    String name = rs.getString("name");
    String description = rs.getString("description");
    BigDecimal price = rs.getBigDecimal("price");
    int left = rs.getInt("left");
    boolean isDeleted = rs.getBoolean("is_deleted");
    BigDecimal rating = rs.getBigDecimal("rating");
    return new Product(id, sellerId, categoryId, name, description, price, left, isDeleted, rating);
  }
}