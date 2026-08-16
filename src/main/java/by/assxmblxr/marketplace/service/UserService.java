package by.assxmblxr.marketplace.service;

import by.assxmblxr.marketplace.exception.ServiceException;
import by.assxmblxr.marketplace.model.User;
import by.assxmblxr.marketplace.model.UserRole;

import java.util.Optional;

/**
 * Business logic for account registration, authentication, and profile management.
 * Orchestrates {@code UserDao} and {@code RememberTokenDao}, and owns password hashing
 * (bcrypt) and remember-me token generation/verification (SHA-256), so no plaintext
 * password or raw token is ever persisted.
 */
public interface UserService {

  /**
   * Registers a new account. The password is hashed before being persisted.
   *
   * @param login    the desired unique login name
   * @param password the plaintext password chosen by the user
   * @param role     the account's role
   * @return the persisted user, with the database-generated id set
   * @throws ServiceException with key {@code error.duplicate_login} if the login is already taken
   */
  User register(String login, String password, UserRole role);

  /**
   * Authenticates a user by login and password. If {@code rememberMe} is {@code true},
   * also issues a remember-me token: a random value is generated, its hash is persisted
   * with a sliding expiration, and the raw value is returned so the caller can set it
   * as a cookie (it is never stored or logged anywhere).
   *
   * @param login      the login to authenticate
   * @param password   the plaintext password to verify
   * @param rememberMe whether to also issue a remember-me token
   * @return the authenticated user, and the raw remember-me token if one was issued
   * @throws ServiceException with key {@code error.invalid_credentials} if the login does not
   *                          exist or the password does not match
   */
  LoginResult login(String login, String password, boolean rememberMe);

  /**
   * Logs out a single device by deleting its remember-me token.
   *
   * @param rawToken the raw token value read from the device's cookie
   */
  void logout(String rawToken);

  /**
   * Authenticates a device by its remember-me token, e.g. when a request arrives with
   * no active session. On success, the token's expiration is extended (sliding
   * expiration), so an active device never needs to log in again. An expired token is
   * deleted and treated as absent.
   *
   * @param rawToken the raw token value read from the device's cookie
   * @return an {@link Optional} containing the authenticated user,
   *         or {@link Optional#empty()} if the token is missing, unknown, or expired
   */
  Optional<User> authenticateByToken(String rawToken);

  /**
   * Changes a user's password after verifying the current one. On success, every
   * remember-me token belonging to the user is deleted, logging out every other
   * device — relevant because a password change is typically prompted by a suspected
   * compromise, so any token an attacker may hold is invalidated too.
   *
   * @param userId      the id of the user changing their password
   * @param oldPassword the current plaintext password, verified before the change is applied
   * @param newPassword the new plaintext password to set
   * @throws ServiceException with key {@code error.invalid_credentials} if the user does not
   *                          exist or {@code oldPassword} does not match
   */
  void changePassword(Long userId, String oldPassword, String newPassword);

  /**
   * Updates a user's login and address. Does not affect the password or role.
   *
   * @param userId  the id of the user to update
   * @param login   the new login name
   * @param address the new postal address
   * @return the updated user
   * @throws ServiceException with key {@code error.invalid_credentials} if no user with the
   *                          given id exists, or {@code error.duplicate_login} if the new login
   *                          is already taken by another user
   */
  User updateProfile(Long userId, String login, String address);
}
