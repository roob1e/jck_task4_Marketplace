package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.ProductDao;
import by.assxmblxr.marketplace.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDaoTest extends AbstractDaoTest {
  private final ProductDao productDao = new ProductDaoImpl();
  private Long sellerId;
  private Long sellerRoleId;
  private Long categoryId;
  private Long productId;

  @BeforeEach
  public void setup() {
    executeUpdate("TRUNCATE users, roles, categories RESTART IDENTITY CASCADE");
    sellerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    sellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller1", "seller");
    categoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Electronics");
    productId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
  }

  @Test
  void testSave() {
    Product product = new Product(null, sellerId, categoryId, "Chip", "M4",
            BigDecimal.valueOf(350), 10, null, null);

    Product actual = productDao.save(product);

    assertNotNull(actual.id());
    assertFalse(actual.isDeleted());
    assertNull(actual.rating());
  }

  @Test
  void testUpdatePositive() {
    BigDecimal newPrice = BigDecimal.valueOf(320);
    Product product = new Product(productId, sellerId, categoryId, "Chip", "M3", newPrice,
            5, false, null);

    Optional<Product> actual = productDao.update(product);

    assertTrue(actual.isPresent());
    assertEquals(newPrice, actual.get().price());
    assertEquals(productId, actual.get().id());
  }

  @Test
  void testUpdateNegative() {
    Product product = new Product(Long.MAX_VALUE, sellerId, categoryId, "Chip", "M3",
            BigDecimal.valueOf(320), 5, false, null);

    Optional<Product> actual = productDao.update(product);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testDeactivatePositive() {
    boolean actual = productDao.deactivate(productId);
    boolean isDeleted = productDao.findById(productId).orElseGet(Assertions::fail).isDeleted();

    assertTrue(actual);
    assertTrue(isDeleted);
  }

  @Test
  void testDeactivateNegativeNonexistentId() {
    boolean actual = productDao.deactivate(Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testDeactivateNegativeAlreadyDeactivated() {
    productDao.deactivate(productId);
    boolean actual = productDao.deactivate(productId);

    assertFalse(actual);
  }

  @Test
  void testFindByIdPositive() {
    Optional<Product> actual = productDao.findById(productId);

    Product p = actual.orElseGet(Assertions::fail);
    assertAll(
            () -> assertEquals(productId, p.id()),
            () -> assertEquals(sellerId, p.sellerId()),
            () -> assertEquals(categoryId, p.categoryId()),
            () -> assertEquals("Chip", p.name()),
            () -> assertEquals("M3", p.description()),
            () -> assertEquals(new BigDecimal("335.50"), p.price()),
            () -> assertEquals(10, p.left()),
            () -> assertFalse(p.isDeleted()),
            () -> assertNull(p.rating())
    );
  }

  @Test
  void testFindByIdNegative() {
    Optional<Product> actual = productDao.findById(Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindAllActiveExcludesDeactivated() {
    Long deactivatedId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
    productDao.deactivate(deactivatedId);
    List<Product> expected = new ArrayList<>();
    expected.add(productDao.findById(productId).orElseGet(Assertions::fail));

    List<Product> actual = productDao.findAllActive(1, 10);

    assertEquals(expected, actual);
  }

  @Test
  void testFindAllActivePagination() {
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, 5.0);
    List<Product> expected1 = new ArrayList<>();
    expected1.add(productDao.findById(otherProductId).orElseGet(Assertions::fail));
    List<Product> expected2 = new ArrayList<>();
    expected2.add(productDao.findById(productId).orElseGet(Assertions::fail));

    List<Product> actual1 = productDao.findAllActive(1, 1);
    List<Product> actual2 = productDao.findAllActive(2, 1);

    assertAll(
            () -> assertEquals(expected1, actual1),
            () -> assertEquals(expected2, actual2)
    );
  }

  @Test
  void testFindAllActiveRatingSorted() {
    productDao.updateRating(productId, BigDecimal.valueOf(4.0));
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, 5.0);
    Long anotherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, null);

    List<Product> expected = new ArrayList<>();
    expected.add(productDao.findById(otherProductId).orElseGet(Assertions::fail));
    expected.add(productDao.findById(productId).orElseGet(Assertions::fail));
    expected.add(productDao.findById(anotherProductId).orElseGet(Assertions::fail));

    List<Product> actual = productDao.findAllActive(1, 10);

    assertEquals(expected, actual);
  }

  @Test
  void testFindAllBySellerPagination() {
    Long secondProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);

    List<Product> expected1 = new ArrayList<>();
    expected1.add(productDao.findById(productId).orElseGet(Assertions::fail));
    List<Product> expected2 = new ArrayList<>();
    expected2.add(productDao.findById(secondProductId).orElseGet(Assertions::fail));

    List<Product> actual1 = productDao.findAllBySeller(sellerId, 1, 1);
    List<Product> actual2 = productDao.findAllBySeller(sellerId, 2, 1);

    assertAll(
            () -> assertEquals(expected1, actual1),
            () -> assertEquals(expected2, actual2)
    );
  }

  @Test
  void testFindAllBySellerExcludesOtherSellers() {
    Long secondSellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller2", "seller");
    insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            secondSellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);

    List<Product> expected = new ArrayList<>();
    expected.add(productDao.findById(productId).orElseGet(Assertions::fail));

    List<Product> actual = productDao.findAllBySeller(sellerId, 1, 10);

    assertEquals(expected, actual);
  }

  @Test
  void testFindAllByCategoryExcludesOtherCategories() {
    Long otherCategoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Books");
    insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, otherCategoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);

    List<Product> expected = new ArrayList<>();
    expected.add(productDao.findById(productId).orElseGet(Assertions::fail));

    List<Product> actual = productDao.findAllByCategory(categoryId, 1, 10);

    assertEquals(expected, actual);
  }

  @Test
  void testFindAllByCategoryPagination() {
    productDao.updateRating(productId, BigDecimal.valueOf(4.0));
    Long secondProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, 5.0);

    List<Product> expected1 = new ArrayList<>();
    expected1.add(productDao.findById(secondProductId).orElseGet(Assertions::fail));
    List<Product> expected2 = new ArrayList<>();
    expected2.add(productDao.findById(productId).orElseGet(Assertions::fail));

    List<Product> actual1 = productDao.findAllByCategory(categoryId, 1, 1);
    List<Product> actual2 = productDao.findAllByCategory(categoryId, 2, 1);

    assertAll(
            () -> assertEquals(expected1, actual1),
            () -> assertEquals(expected2, actual2)
    );
  }

  @Test
  void testFindAllByCategoryRatingSorted() {
    productDao.updateRating(productId, BigDecimal.valueOf(4.0));
    Long higherRatedId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, 5.0);
    Long unratedId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\", rating)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10, null);

    List<Product> expected = new ArrayList<>();
    expected.add(productDao.findById(higherRatedId).orElseGet(Assertions::fail));
    expected.add(productDao.findById(productId).orElseGet(Assertions::fail));
    expected.add(productDao.findById(unratedId).orElseGet(Assertions::fail));

    List<Product> actual = productDao.findAllByCategory(categoryId, 1, 10);

    assertEquals(expected, actual);
  }

  @Test
  void testUpdateRatingPositive() {
    BigDecimal newRating = BigDecimal.valueOf(4.5);

    productDao.updateRating(productId, newRating);
    BigDecimal actual = productDao.findById(productId).orElseGet(Assertions::fail).rating();

    assertEquals(0, newRating.compareTo(actual));
  }

  @Test
  void testUpdateRatingNonexistentIdDoesNotThrow() {
    assertDoesNotThrow(() -> productDao.updateRating(Long.MAX_VALUE, BigDecimal.valueOf(4.5)));
  }
}