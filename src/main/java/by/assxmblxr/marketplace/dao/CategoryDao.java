package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
  Optional<Category> findById(Long id);
  List<Category> findAll();
}