package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.CategoryDao;
import by.assxmblxr.marketplace.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDaoTest extends AbstractDaoTest {
  private final CategoryDao categoryDao = new CategoryDaoImpl();
  private Long electronicsId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE TABLE categories RESTART IDENTITY CASCADE");
    electronicsId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id",
            "Electronics");
    insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id",
            "Books");
  }

  @Test
  void testFindByIdPositive() {
    String expectedName = "Electronics";

    Category actual = categoryDao.findById(electronicsId).orElseGet(Assertions::fail);
    assertAll(
            () -> assertEquals(electronicsId, actual.id()),
            () -> assertEquals(expectedName, actual.name())
    );
  }

  @Test
  void testFindByIdNegative() {
    Optional<Category> actual = categoryDao.findById(Long.MAX_VALUE);
    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindAllPositive() {
    List<Category> expected = new ArrayList<>();
    expected.add(new Category(2L, "Books"));
    expected.add(new Category(1L, "Electronics"));

    List<Category> actual = categoryDao.findAll();
    assertEquals(expected, actual);
  }

  @Test
  void testFindAllNegative() {
    executeUpdate("TRUNCATE TABLE categories RESTART IDENTITY CASCADE");

    List<Category> actual = categoryDao.findAll();

    assertAll(
            () -> assertNotNull(actual),
            () -> assertTrue(actual.isEmpty())
    );
  }
}