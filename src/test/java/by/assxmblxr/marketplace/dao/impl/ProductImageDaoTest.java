package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.ProductImageDao;
import by.assxmblxr.marketplace.model.ProductImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ProductImageDaoTest extends AbstractDaoTest {
  private final ProductImageDao productImageDao = new ProductImageDaoImpl();
  private Long productId;
  private Long mainImageId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles, categories RESTART IDENTITY CASCADE");
    Long sellerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    Long sellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller1", "seller");
    Long categoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Electronics");
    productId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
    mainImageId = insertAndGetId("INSERT INTO product_images (product_id, path, \"order\") VALUES (?, ?, ?) RETURNING id",
            productId, "uuid-main", 1);
  }

  @Test
  void testSave() {
    ProductImage image = new ProductImage(null, productId, "uuid-second", 2);

    ProductImage actual = productImageDao.save(image);

    assertAll(
            () -> assertNotNull(actual.id()),
            () -> assertEquals(productId, actual.productId()),
            () -> assertEquals("uuid-second", actual.path()),
            () -> assertEquals(2, actual.order())
    );
  }

  @Test
  void testUpdatePositive() {
    ProductImage image = new ProductImage(mainImageId, productId, "uuid-updated", 3);

    Optional<ProductImage> actual = productImageDao.update(image);

    assertTrue(actual.isPresent());
    assertEquals("uuid-updated", actual.get().path());
    assertEquals(3, actual.get().order());
  }

  @Test
  void testUpdateNegative() {
    ProductImage image = new ProductImage(Long.MAX_VALUE, productId, "uuid-updated", 3);

    Optional<ProductImage> actual = productImageDao.update(image);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testDeletePositive() {
    boolean actual = productImageDao.delete(mainImageId);
    Optional<ProductImage> afterDelete = productImageDao.findById(mainImageId);

    assertTrue(actual);
    assertTrue(afterDelete.isEmpty());
  }

  @Test
  void testDeleteNegative() {
    boolean actual = productImageDao.delete(Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testFindByIdPositive() {
    ProductImage actual = productImageDao.findById(mainImageId).orElseGet(Assertions::fail);

    assertAll(
            () -> assertEquals(mainImageId, actual.id()),
            () -> assertEquals(productId, actual.productId()),
            () -> assertEquals("uuid-main", actual.path()),
            () -> assertEquals(1, actual.order())
    );
  }

  @Test
  void testFindByIdNegative() {
    Optional<ProductImage> actual = productImageDao.findById(Long.MAX_VALUE);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindMainPhotoPositive() {
    Optional<ProductImage> actual = productImageDao.findMainPhoto(productId);

    assertTrue(actual.isPresent());
    assertEquals(mainImageId, actual.get().id());
  }

  @Test
  void testFindMainPhotoNegative() {
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES ((SELECT seller_id FROM products WHERE id = ?), (SELECT category_id FROM products WHERE id = ?)," +
                    " ?, ?, ?, ?) RETURNING id",
            productId, productId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);

    Optional<ProductImage> actual = productImageDao.findMainPhoto(otherProductId);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testFindAllByProductId() {
    Long secondImageId = insertAndGetId("INSERT INTO product_images (product_id, path, \"order\") VALUES (?, ?, ?) RETURNING id",
            productId, "uuid-second", 2);

    List<ProductImage> actual = productImageDao.findAllByProductId(productId);

    assertEquals(2, actual.size());
    assertEquals(mainImageId, actual.get(0).id());
    assertEquals(secondImageId, actual.get(1).id());
  }

  @Test
  void testFindAllMainPhotos() {
    Long otherProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES ((SELECT seller_id FROM products WHERE id = ?), (SELECT category_id FROM products WHERE id = ?)," +
                    " ?, ?, ?, ?) RETURNING id",
            productId, productId, "Board", "M3", BigDecimal.valueOf(120), 5);
    Long otherMainImageId = insertAndGetId("INSERT INTO product_images (product_id, path, \"order\") VALUES (?, ?, ?) RETURNING id",
            otherProductId, "uuid-other-main", 1);

    Map<Long, ProductImage> actual = productImageDao.findAllMainPhotos(List.of(productId, otherProductId));

    assertEquals(2, actual.size());
    assertEquals(mainImageId, actual.get(productId).id());
    assertEquals(otherMainImageId, actual.get(otherProductId).id());
  }
}
