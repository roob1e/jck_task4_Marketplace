package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.RememberTokenDao;
import by.assxmblxr.marketplace.model.RememberToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RememberTokenDaoTest extends AbstractDaoTest {
  private final RememberTokenDao rememberTokenDao = new RememberTokenDaoImpl();
  private Long userId;
  private String tokenHash;
  private OffsetDateTime expiresAt;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles RESTART IDENTITY CASCADE");
    Long roleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    userId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            roleId, "buyer1", "hash");
    tokenHash = "hash1";
    expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusWeeks(2);
    executeUpdate("INSERT INTO remember_tokens (token_hash, user_id, expires_at) VALUES (?, ?, ?)",
            tokenHash, userId, expiresAt);
  }

  @Test
  void testSave() {
    String newTokenHash = "hash2";
    OffsetDateTime newExpiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusWeeks(2);
    RememberToken token = new RememberToken(newTokenHash, userId, newExpiresAt);

    RememberToken actual = rememberTokenDao.save(token);

    assertAll(
            () -> assertEquals(newTokenHash, actual.tokenHash()),
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(newExpiresAt, actual.expiresAt())
    );
  }

  @Test
  void testFindByTokenHashPositive() {
    RememberToken actual = rememberTokenDao.findByTokenHash(tokenHash).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(tokenHash, actual.tokenHash()),
            () -> assertEquals(userId, actual.userId()),
            () -> assertEquals(expiresAt, actual.expiresAt())
    );
  }

  @Test
  void testFindByTokenHashNegative() {
    Optional<RememberToken> actual = rememberTokenDao.findByTokenHash("nonexistent");

    assertTrue(actual.isEmpty());
  }

  @Test
  void testExtendExpirationPositive() {
    OffsetDateTime newExpiresAt = expiresAt.plusWeeks(2);

    boolean actual = rememberTokenDao.extendExpiration(tokenHash, newExpiresAt);
    OffsetDateTime actualExpiresAt = rememberTokenDao.findByTokenHash(tokenHash).orElseGet(Assertions::fail).expiresAt();

    assertTrue(actual);
    assertEquals(newExpiresAt, actualExpiresAt);
  }

  @Test
  void testExtendExpirationNegative() {
    boolean actual = rememberTokenDao.extendExpiration("nonexistent", OffsetDateTime.now(ZoneOffset.UTC));

    assertFalse(actual);
  }

  @Test
  void testDeleteByTokenHashPositive() {
    boolean actual = rememberTokenDao.deleteByTokenHash(tokenHash);

    assertTrue(actual);
    assertTrue(rememberTokenDao.findByTokenHash(tokenHash).isEmpty());
  }

  @Test
  void testDeleteByTokenHashNegative() {
    boolean actual = rememberTokenDao.deleteByTokenHash("nonexistent");

    assertFalse(actual);
  }

  @Test
  void testDeleteAllByUserIdPositive() {
    executeUpdate("INSERT INTO remember_tokens (token_hash, user_id, expires_at) VALUES (?, ?, ?)",
            "hash2", userId, expiresAt);

    int actual = rememberTokenDao.deleteAllByUserId(userId);

    assertEquals(2, actual);
    assertTrue(rememberTokenDao.findByTokenHash(tokenHash).isEmpty());
  }

  @Test
  void testDeleteAllByUserIdNegative() {
    int actual = rememberTokenDao.deleteAllByUserId(Long.MAX_VALUE);

    assertEquals(0, actual);
  }
}
