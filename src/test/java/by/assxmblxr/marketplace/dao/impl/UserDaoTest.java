package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.UserDao;
import by.assxmblxr.marketplace.model.Role;
import by.assxmblxr.marketplace.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest extends AbstractDaoTest {
  private final UserDao userDao = new UserDaoImpl();
  private Long userId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles RESTART IDENTITY CASCADE");
    Long buyerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    userId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "user1", "user");
  }

  @Test
  void testSave() {
    User user = new User(null, Role.BUYER, "user2", "user", null);
    User actual = userDao.save(user);

    assertAll(
            () -> assertNotNull(actual.id()),
            () -> assertEquals(user.role(), actual.role()),
            () -> assertEquals(user.login(), actual.login()),
            () -> assertEquals(user.passwordHash(), actual.passwordHash()),
            () -> assertEquals(user.address(), actual.address())
    );
  }

  @Test
  void testUpdatePositive() {
    String newLogin = "newLogin";
    User user = new User(userId, Role.BUYER, newLogin, "user", null);

    Optional<User> updated = userDao.update(user);

    User u = updated.orElseGet(Assertions::fail);
    assertAll(
            () -> assertEquals(newLogin, u.login()),
            () -> assertEquals(userId, u.id())
    );
  }

  @Test
  void testUpdateNegative() {
    User user = new User(Long.MAX_VALUE, Role.BUYER, "user1", "user", null);

    Optional<User> updated = userDao.update(user);
    assertTrue(updated.isEmpty());
  }

  @Test
  void testDeletePositive() {
    boolean actual = userDao.delete(userId);
    Optional<User> actualUser = userDao.findById(userId);

    assertAll(
            () -> assertTrue(actual),
            () -> assertTrue(actualUser.isEmpty())
    );
  }

  @Test
  void testDeleteNegative() {
    boolean actual = userDao.delete(Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testFindByIdPositive() {
    Optional<User> actual = userDao.findById(userId);
    User u = actual.orElseGet(Assertions::fail);
    assertAll(
            () -> assertEquals(userId, u.id()),
            () -> assertEquals(Role.BUYER, u.role()),
            () -> assertEquals("user1", u.login())
    );
  }

  @Test
  void testFindByIdNegative() {
    Optional<User> actual = userDao.findById(Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindByLoginPositive() {
    String login = "user1";

    Optional<User> actual = userDao.findByLogin(login);
    User u = actual.orElseGet(Assertions::fail);
    assertAll(
            () -> assertEquals(1L, u.id()),
            () -> assertEquals(login, u.login()),
            () -> assertEquals(Role.BUYER, u.role())
    );
  }

  @Test
  void testFindByLoginNegative() {
    String login = "nonexistent_login";

    Optional<User> actual = userDao.findByLogin(login);

    assertTrue(actual.isEmpty());
  }
}