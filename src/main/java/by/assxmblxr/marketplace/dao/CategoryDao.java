package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Read-only data access interface for {@link Category} entities.
 * Categories are seeded/administered outside the application, so no
 * create/update/delete operations are exposed here.
 */
public interface CategoryDao {

  /**
   * Finds a category by its primary key.
   *
   * @param id the category id to look up
   * @return an {@link Optional} containing the matching category,
   *         or {@link Optional#empty()} if no category with the given id exists
   */
  Optional<Category> findById(Long id);

  /**
   * Returns all categories, ordered alphabetically by name.
   *
   * @return every category in the {@code categories} table; an empty list if none exist
   */
  List<Category> findAll();
}