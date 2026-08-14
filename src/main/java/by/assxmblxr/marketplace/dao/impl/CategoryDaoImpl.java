package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.CategoryDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {
  private final ConnectionPool pool;

  public CategoryDaoImpl() {
    this.pool = ConnectionPool.getInstance();
  }

  @Override
  public Optional<Category> findById(Long id) {
    String sql = """
            SELECT * FROM categories
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
  public List<Category> findAll() {
    String sql = """
            SELECT * FROM categories
            ORDER BY name;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Category> categories = new ArrayList<>();
      while (rs.next()) {
        categories.add(mapFromResultSet(rs));
      }
      return categories;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private Category mapFromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong("id");
    String name = rs.getString("name");
    return new Category(id, name);
  }
}
