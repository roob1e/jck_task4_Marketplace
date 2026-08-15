package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractDaoTest {
  protected void executeUpdate(String sql, Object... params) {
    try (Connection conn = ConnectionPool.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);
      }
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new AssertionError(e);
    }
  }

  protected Long insertAndGetId(String sql, Object... params) {
    try (Connection conn = ConnectionPool.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("id");
        } else {
          throw new AssertionError("No id returned");
        }
      }
    } catch (SQLException e) {
      throw new AssertionError(e);
    }
  }
}