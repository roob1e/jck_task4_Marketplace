package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.UserDao;
import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.model.Role;
import by.assxmblxr.marketplace.model.User;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
  private final ConnectionPool pool;

  public UserDaoImpl() {
    this.pool = ConnectionPool.getInstance();
  }

  @Override
  public User save(User user) {
    String sql = """
            INSERT INTO users (role_id, login, password_hash, address)
            values ((SELECT id FROM roles WHERE name = ?), ?, ?, ?);
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, user.role().name());
      ps.setString(2, user.login());
      ps.setString(3, user.passwordHash());
      ps.setString(4, user.address());
      ps.execute();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          long id = rs.getLong(1);
          user = new User(id, user.role(), user.login(), user.passwordHash(), user.address());
        }
      }
      return user;
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<User> update(User user) {
    Objects.requireNonNull(user.id(), "Cannot update a user without an id");
    String sql = """
            UPDATE users
            SET role_id = (SELECT id FROM roles WHERE name = ?), login = ?, password_hash = ?, address = ?
            WHERE id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, user.role().name());
      ps.setString(2, user.login());
      ps.setString(3, user.passwordHash());
      ps.setString(4, user.address());
      ps.setLong(5, user.id());
      int rows = ps.executeUpdate();
      if (rows > 0) {
        return Optional.of(user);
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
            DELETE FROM users WHERE id = ?;
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
  public Optional<User> findById(Long id) {
    String sql = """
            SELECT u.id, u.login, u.password_hash, u.address, r.name AS role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.id = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return mapFromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<User> findByLogin(String login) {
    String sql = """
            SELECT u.id, u.login, u.password_hash, u.address, r.name AS role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.login = ?;
            """;
    try (Connection conn = pool.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, login);
      try (ResultSet rs = ps.executeQuery()) {
        return mapFromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new DaoException(e.getMessage(), e);
    }
  }

  private Optional<User> mapFromResultSet(ResultSet rs) throws SQLException {
    if (rs.next()) {
      Long id = rs.getLong("id");
      String login = rs.getString("login");
      String passwordHash = rs.getString("password_hash");
      String address = rs.getString("address");
      String roleName = rs.getString("role_name");
      return Optional.of(new User(id, Role.valueOf(roleName), login, passwordHash, address));
    }
    return Optional.empty();
  }
}
