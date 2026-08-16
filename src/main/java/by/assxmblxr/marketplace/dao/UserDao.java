package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.User;

import java.util.Optional;

/**
 * Data access interface for {@link User} entities.
 */
public interface UserDao {

  /**
   * Persists a new user. The {@code id} of the given user is ignored; it is generated
   * by the database and returned in the result.
   *
   * @param user the user to create; {@code id} may be {@code null}
   * @return the persisted user, with the database-generated {@code id} set
   */
  User save(User user);

  /**
   * Updates an existing user's userRole, login, password hash, and address.
   *
   * @param user the user data to write, identified by {@link User#id()}
   * @return an {@link Optional} containing the updated user if a user with the given id
   *         was found, or {@link Optional#empty()} otherwise
   */
  Optional<User> update(User user);

  /**
   * Deletes a user by id.
   *
   * @param id the id of the user to delete
   * @return {@code true} if a user with the given id was found and deleted, {@code false} otherwise
   */
  boolean delete(Long id);

  /**
   * Finds a user by their primary key.
   *
   * @param id the user id to look up
   * @return an {@link Optional} containing the matching user,
   *         or {@link Optional#empty()} if no user with the given id exists
   */
  Optional<User> findById(Long id);

  /**
   * Finds a user by their login name. Used primarily during authentication.
   *
   * @param login the login to look up
   * @return an {@link Optional} containing the matching user,
   *         or {@link Optional#empty()} if no user with the given login exists
   */
  Optional<User> findByLogin(String login);
}