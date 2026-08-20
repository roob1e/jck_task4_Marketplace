package by.assxmblxr.marketplace.service.impl;

import by.assxmblxr.marketplace.dao.RememberTokenDao;
import by.assxmblxr.marketplace.dao.UserDao;
import by.assxmblxr.marketplace.exception.DaoException;
import by.assxmblxr.marketplace.exception.ServiceException;
import by.assxmblxr.marketplace.model.RememberToken;
import by.assxmblxr.marketplace.model.User;
import by.assxmblxr.marketplace.model.UserRole;
import by.assxmblxr.marketplace.service.LoginResult;
import by.assxmblxr.marketplace.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

public class UserServiceImpl implements UserService {
  private final UserDao userDao;
  private final RememberTokenDao tokenDao;

  public UserServiceImpl(UserDao userDao, RememberTokenDao tokenDao) {
    this.userDao = userDao;
    this.tokenDao = tokenDao;
  }

  @Override
  public User register(String login, String password, UserRole role) {
    String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    User user = new User(null, role, login, passwordHash, null);
    try {
      return userDao.save(user);
    } catch (DaoException e) {
      if (e.getCause() instanceof SQLException err && "23505".equals(err.getSQLState())) {
        throw new ServiceException("error.duplicate_login");
      }
      throw e;
    }
  }

  @Override
  public LoginResult login(String login, String password, boolean rememberMe) {
    User user = userDao.findByLogin(login)
            .orElseThrow(() -> new ServiceException("error.invalid_credentials"));
    if (!BCrypt.checkpw(password, user.passwordHash())) {
      throw new ServiceException("error.invalid_credentials");
    }
    if (rememberMe) {
      try {
        SecureRandom random = new SecureRandom();
        byte[] rawBytes = new byte[32];
        random.nextBytes(rawBytes);
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawBytes);


        String tokenHash = hashToken(rawToken);
        RememberToken token = new RememberToken(tokenHash, user.id(),
                OffsetDateTime.now(ZoneOffset.UTC).plusWeeks(1));
        tokenDao.save(token);

        return new LoginResult(user, rawToken);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    } else {
      return new LoginResult(user, null);
    }
  }

  @Override
  public void logout(String rawToken) {
    try {
      String tokenHash = hashToken(rawToken);
      tokenDao.deleteByTokenHash(tokenHash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<User> authenticateByToken(String rawToken) {
    try {
      String tokenHash = hashToken(rawToken);
      Optional<RememberToken> token = tokenDao.findByTokenHash(tokenHash);
      if (token.isPresent()) {
        RememberToken rememberToken = token.get();
        if (rememberToken.expiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
          tokenDao.deleteByTokenHash(tokenHash);
          return Optional.empty();
        }
        tokenDao.extendExpiration(tokenHash, OffsetDateTime.now(ZoneOffset.UTC).plusWeeks(1));
        return userDao.findById(rememberToken.userId());
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return Optional.empty();
  }

  @Override
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    User user = userDao.findById(userId)
            .orElseThrow(() -> new ServiceException("error.invalid_credentials"));
    if (!BCrypt.checkpw(oldPassword, user.passwordHash())) {
      throw new ServiceException("error.invalid_credentials");
    }
    if (oldPassword.equals(newPassword)) {
      return;
    }
    String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    User updatedPassword = new User(userId, user.userRole(), user.login(), newPasswordHash, user.address());
    userDao.update(updatedPassword)
            .orElseThrow(() -> new ServiceException("error.invalid_credentials"));
    tokenDao.deleteAllByUserId(userId);
  }

  @Override
  public User updateProfile(Long userId, String login, String address) {
    User user = userDao.findById(userId)
            .orElseThrow(() -> new ServiceException("error.invalid_credentials"));
    User updatedUser = new User(userId, user.userRole(), login, user.passwordHash(), address);
    try {
      return userDao.update(updatedUser)
              .orElseThrow(() -> new ServiceException("error.invalid_credentials"));
    } catch (DaoException e) {
      if (e.getCause() instanceof SQLException err && "23505".equals(err.getSQLState())) {
        throw new ServiceException("error.duplicate_login");
      }
      throw e;
    }
  }

  private String hashToken(String rawToken) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hashBytes);
  }
}
