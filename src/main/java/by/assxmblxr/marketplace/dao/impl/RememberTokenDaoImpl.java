package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.RememberTokenDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.RememberToken;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;

public class RememberTokenDaoImpl implements RememberTokenDao {
  private final ConnectionPool pool;
  
  public RememberTokenDaoImpl() {
    pool = ConnectionPool.getInstance();
  }
  
  @Override
  public RememberToken save(RememberToken token) {
    String sql = """
            INSERT INTO remember_tokens (token_hash, user_id, expires_at)
            VALUES (?, ?, ?);
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, token.tokenHash());
      ps.setLong(2, token.userId());
      ps.setObject(3, token.expiresAt());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return token;
      } else {
        throw new DaoException("Failed to create rememberToken");
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<RememberToken> findByTokenHash(String tokenHash) {
    String sql = """
            SELECT * FROM remember_tokens
            WHERE token_hash = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
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
  public boolean extendExpiration(String tokenHash, OffsetDateTime newExpiresAt) {
    String sql = """
            UPDATE remember_tokens
            SET expires_at = ?
            WHERE token_hash = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setObject(1, newExpiresAt);
      ps.setString(2, tokenHash);
      int rows = ps.executeUpdate();
      return rows == 1;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public boolean deleteByTokenHash(String tokenHash) {
    String sql = """
            DELETE FROM remember_tokens
            WHERE token_hash = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      int rows = ps.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public int deleteAllByUserId(Long userId) {
    String sql = """
            DELETE FROM remember_tokens
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

  private RememberToken mapFromResultSet(ResultSet rs) throws SQLException {
    String tokenHash = rs.getString("token_hash");
    Long userId = rs.getLong("user_id");
    OffsetDateTime expiresAt = rs.getObject("expires_at", OffsetDateTime.class);
    return new RememberToken(tokenHash, userId, expiresAt);
  }
}
