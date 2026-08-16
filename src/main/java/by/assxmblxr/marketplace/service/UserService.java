package by.assxmblxr.marketplace.service;

import by.assxmblxr.marketplace.model.User;

public interface UserService {
  User login(String username, String password);
  User register(User user);
}
