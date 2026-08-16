package by.assxmblxr.marketplace.service.impl;

import by.assxmblxr.marketplace.dao.RememberTokenDao;
import by.assxmblxr.marketplace.dao.UserDao;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.exception.ServiceException;
import by.assxmblxr.marketplace.model.RememberToken;
import by.assxmblxr.marketplace.model.User;
import by.assxmblxr.marketplace.model.UserRole;
import by.assxmblxr.marketplace.service.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock
  private UserDao userDao;
  @Mock
  private RememberTokenDao tokenDao;

  private UserServiceImpl userService;
  private User user;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userDao, tokenDao);
    user = new User(1L, UserRole.BUYER, "buyer1", BCrypt.hashpw("password", BCrypt.gensalt()), "Minsk");
  }

  @Test
  void testRegisterSuccess() {
    when(userDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    User actual = userService.register("newLogin", "rawPassword", UserRole.BUYER);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userDao).save(captor.capture());
    User savedArg = captor.getValue();
    assertAll(
            () -> assertEquals("newLogin", actual.login()),
            () -> assertEquals(UserRole.BUYER, actual.userRole()),
            () -> assertTrue(BCrypt.checkpw("rawPassword", savedArg.passwordHash()))
    );
  }

  @Test
  void testRegisterDuplicateLogin() {
    SQLException sqlException = mock(SQLException.class);
    when(sqlException.getSQLState()).thenReturn("23505");
    when(userDao.save(any())).thenThrow(new DaoException("duplicate", sqlException));

    ServiceException actual = assertThrows(ServiceException.class,
            () -> userService.register("newLogin", "rawPassword", UserRole.BUYER));

    assertEquals("error.duplicate_login", actual.getMessageKey());
  }

  @Test
  void testRegisterOtherDaoExceptionPropagates() {
    SQLException sqlException = mock(SQLException.class);
    when(sqlException.getSQLState()).thenReturn("08000");
    when(userDao.save(any())).thenThrow(new DaoException("connection failure", sqlException));

    assertThrows(DaoException.class, () -> userService.register("newLogin", "rawPassword", UserRole.BUYER));
  }

  @Test
  void testLoginSuccessNoRememberMe() {
    when(userDao.findByLogin("buyer1")).thenReturn(Optional.of(user));

    LoginResult actual = userService.login("buyer1", "password", false);

    assertAll(
            () -> assertEquals(user, actual.user()),
            () -> assertNull(actual.token())
    );
    verifyNoInteractions(tokenDao);
  }

  @Test
  void testLoginSuccessWithRememberMe() throws NoSuchAlgorithmException {
    when(userDao.findByLogin("buyer1")).thenReturn(Optional.of(user));

    LoginResult actual = userService.login("buyer1", "password", true);

    assertNotNull(actual.token());
    ArgumentCaptor<RememberToken> captor = ArgumentCaptor.forClass(RememberToken.class);
    verify(tokenDao).save(captor.capture());
    RememberToken saved = captor.getValue();
    assertAll(
            () -> assertEquals(hashToken(actual.token()), saved.tokenHash()),
            () -> assertEquals(user.id(), saved.userId()),
            () -> assertTrue(saved.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).plusDays(6))),
            () -> assertTrue(saved.expiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC).plusDays(8)))
    );
  }

  @Test
  void testLoginInvalidLogin() {
    when(userDao.findByLogin("unknown")).thenReturn(Optional.empty());

    ServiceException actual = assertThrows(ServiceException.class,
            () -> userService.login("unknown", "password", false));

    assertEquals("error.invalid_credentials", actual.getMessageKey());
    verifyNoInteractions(tokenDao);
  }

  @Test
  void testLoginInvalidPassword() {
    when(userDao.findByLogin("buyer1")).thenReturn(Optional.of(user));

    ServiceException actual = assertThrows(ServiceException.class,
            () -> userService.login("buyer1", "wrongPassword", false));

    assertEquals("error.invalid_credentials", actual.getMessageKey());
  }

  @Test
  void testLogout() throws NoSuchAlgorithmException {
    String rawToken = "rawToken";

    userService.logout(rawToken);

    verify(tokenDao).deleteByTokenHash(hashToken(rawToken));
  }

  @Test
  void testAuthenticateByTokenNotFound() {
    when(tokenDao.findByTokenHash(any())).thenReturn(Optional.empty());

    Optional<User> actual = userService.authenticateByToken("rawToken");

    assertTrue(actual.isEmpty());
    verifyNoInteractions(userDao);
  }

  @Test
  void testAuthenticateByTokenExpired() throws NoSuchAlgorithmException {
    String rawToken = "rawToken";
    RememberToken expired = new RememberToken(hashToken(rawToken), user.id(),
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
    when(tokenDao.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.of(expired));

    Optional<User> actual = userService.authenticateByToken(rawToken);

    assertTrue(actual.isEmpty());
    verify(tokenDao).deleteByTokenHash(hashToken(rawToken));
    verify(tokenDao, never()).extendExpiration(any(), any());
    verifyNoInteractions(userDao);
  }

  @Test
  void testAuthenticateByTokenValid() throws NoSuchAlgorithmException {
    String rawToken = "rawToken";
    RememberToken valid = new RememberToken(hashToken(rawToken), user.id(),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
    when(tokenDao.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.of(valid));
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));

    Optional<User> actual = userService.authenticateByToken(rawToken);

    assertEquals(Optional.of(user), actual);
    verify(tokenDao).extendExpiration(eq(hashToken(rawToken)), any());
    verify(tokenDao, never()).deleteByTokenHash(any());
  }

  @Test
  void testChangePasswordSuccess() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));
    when(userDao.update(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

    userService.changePassword(user.id(), "password", "newPassword");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userDao).update(captor.capture());
    assertTrue(BCrypt.checkpw("newPassword", captor.getValue().passwordHash()));
    verify(tokenDao).deleteAllByUserId(user.id());
  }

  @Test
  void testChangePasswordInvalidOldPassword() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));

    ServiceException actual = assertThrows(ServiceException.class,
            () -> userService.changePassword(user.id(), "wrongPassword", "newPassword"));

    assertEquals("error.invalid_credentials", actual.getMessageKey());
    verify(userDao, never()).update(any());
    verifyNoInteractions(tokenDao);
  }

  @Test
  void testChangePasswordSameAsOld() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));

    userService.changePassword(user.id(), "password", "password");

    verify(userDao, never()).update(any());
    verifyNoInteractions(tokenDao);
  }

  @Test
  void testChangePasswordUserNotFound() {
    when(userDao.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

    assertThrows(ServiceException.class,
            () -> userService.changePassword(Long.MAX_VALUE, "password", "newPassword"));
  }

  @Test
  void testChangePasswordUpdateNotFound() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));
    when(userDao.update(any())).thenReturn(Optional.empty());

    assertThrows(ServiceException.class,
            () -> userService.changePassword(user.id(), "password", "newPassword"));

    verify(tokenDao, never()).deleteAllByUserId(any());
  }

  @Test
  void testUpdateProfileSuccess() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));
    when(userDao.update(any())).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

    User actual = userService.updateProfile(user.id(), "newLogin", "newAddress");

    assertAll(
            () -> assertEquals("newLogin", actual.login()),
            () -> assertEquals("newAddress", actual.address()),
            () -> assertEquals(user.passwordHash(), actual.passwordHash())
    );
  }

  @Test
  void testUpdateProfileDuplicateLogin() {
    when(userDao.findById(user.id())).thenReturn(Optional.of(user));
    SQLException sqlException = mock(SQLException.class);
    when(sqlException.getSQLState()).thenReturn("23505");
    when(userDao.update(any())).thenThrow(new DaoException("duplicate", sqlException));

    ServiceException actual = assertThrows(ServiceException.class,
            () -> userService.updateProfile(user.id(), "takenLogin", "newAddress"));

    assertEquals("error.duplicate_login", actual.getMessageKey());
  }

  @Test
  void testUpdateProfileUserNotFound() {
    when(userDao.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

    assertThrows(ServiceException.class,
            () -> userService.updateProfile(Long.MAX_VALUE, "newLogin", "newAddress"));
  }

  private String hashToken(String rawToken) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
  }
}
