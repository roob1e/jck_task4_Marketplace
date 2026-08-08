package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.User;

import java.util.Optional;

public interface UserDao {
  User save(User user);
  Optional<User> update(User user);
  boolean delete(Long id);
  Optional<User> findById(Long id);
  Optional<User> findByLogin(String login);
}