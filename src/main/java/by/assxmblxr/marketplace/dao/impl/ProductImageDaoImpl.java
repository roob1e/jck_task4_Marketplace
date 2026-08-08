package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.ProductImageDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.ProductImage;

import java.sql.*;
import java.util.*;

public class ProductImageDaoImpl implements ProductImageDao {
  private final ConnectionPool pool;

  public ProductImageDaoImpl() {
    this.pool = ConnectionPool.getInstance();
  }

  @Override
  public ProductImage save(ProductImage image) {
    String sql = """
            INSERT INTO product_images (product_id, path, "order")
            VALUES (?, ?, ?)
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, image.productId());
      ps.setString(2, image.path());
      ps.setInt(3, image.order());
      ps.execute();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          long id = rs.getLong(1);
          image = new ProductImage(id, image.productId(), image.path(), image.order());
        }
      }
      return image;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<ProductImage> update(ProductImage image) {
    Objects.requireNonNull(image.id(), "Cannot update an image without an id");
    String sql = """
            UPDATE product_images
            SET path = ?, "order" = ?
            WHERE id=?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, image.path());
      ps.setInt(2, image.order());
      ps.setLong(3, image.id());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return Optional.of(image);
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
            DELETE FROM product_images
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<ProductImage> findById(Long id) {
    String sql = """
            SELECT * FROM product_images WHERE id=?;
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
  public Optional<ProductImage> findMainPhoto(Long productId) {
    String sql = """
            SELECT * FROM product_images
            WHERE product_id = ? AND "order" = 1;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
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
  public List<ProductImage> findAllByProductId(Long productId) {
    String sql = """
            SELECT * FROM product_images
            WHERE product_id = ?
            ORDER BY "order";
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductImage> images = new ArrayList<>();
        while (rs.next()) {
          ProductImage image = mapFromResultSet(rs);
          images.add(image);
        }
        return images;
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Map<Long, ProductImage> findAllMainPhotos(List<Long> productIds) {
    String sql = """
            SELECT * FROM product_images
            WHERE product_id = ANY(?) AND "order" = 1;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      Array sqlArray = conn.createArrayOf("bigint", productIds.toArray());
      ps.setArray(1, sqlArray);
      try (ResultSet rs = ps.executeQuery()) {
        Map<Long, ProductImage> images = new HashMap<>();
        while (rs.next()) {
          ProductImage image = mapFromResultSet(rs);
          images.put(image.productId(), image);
        }
        return images;
      } finally {
        sqlArray.free();
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    } finally {
    }
  }

  private ProductImage mapFromResultSet(ResultSet rs) throws SQLException {
    long id = rs.getLong("id");
    long productId = rs.getLong("product_id");
    String path = rs.getString("path");
    int order = rs.getInt("order");
    return new ProductImage(id, productId, path, order);
  }
}
