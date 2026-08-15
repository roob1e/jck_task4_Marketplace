package by.assxmblxr.marketplace.dao.impl;

import by.assxmblxr.marketplace.dao.AbstractDaoTest;
import by.assxmblxr.marketplace.dao.ReviewDao;
import by.assxmblxr.marketplace.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewDaoTest extends AbstractDaoTest {
  private final ReviewDao reviewDao = new ReviewDaoImpl();
  private Long buyerRoleId;
  private Long sellerId;
  private Long categoryId;
  private Long productId;
  private Long buyerId;
  private Long orderItemId;
  private Long reviewId;

  @BeforeEach
  public void setUp() {
    executeUpdate("TRUNCATE users, roles, categories RESTART IDENTITY CASCADE");
    buyerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "BUYER");
    Long sellerRoleId = insertAndGetId("INSERT INTO roles (name) VALUES (?) RETURNING id", "SELLER");
    sellerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            sellerRoleId, "seller1", "hash");
    categoryId = insertAndGetId("INSERT INTO categories (name) VALUES (?) RETURNING id", "Electronics");
    productId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Chip", "M3", BigDecimal.valueOf(335.5), 10);
    buyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "buyer1", "hash");
    Long orderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            buyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    orderItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            orderId, productId, 1, BigDecimal.valueOf(335.5), "ACTIVE");
    reviewId = insertAndGetId("INSERT INTO reviews (order_item_id, user_id, product_id, rating, created_at, description)" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            orderItemId, buyerId, productId, 4, OffsetDateTime.now(ZoneOffset.UTC), "Good chip");
  }

  private Long createAdditionalReview(int rating) {
    Long newBuyerId = insertAndGetId("INSERT INTO users (role_id, login, password_hash) VALUES (?, ?, ?) RETURNING id",
            buyerRoleId, "buyer" + rating + System.nanoTime(), "hash");
    Long newOrderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            newBuyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    Long newOrderItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            newOrderId, productId, 1, BigDecimal.valueOf(335.5), "ACTIVE");
    return insertAndGetId("INSERT INTO reviews (order_item_id, user_id, product_id, rating, created_at, description)" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            newOrderItemId, newBuyerId, productId, rating, OffsetDateTime.now(ZoneOffset.UTC), "review");
  }

  @Test
  void testSave() {
    Long secondProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Board", "M3", BigDecimal.valueOf(120), 5);
    Long secondOrderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            buyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    Long secondOrderItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            secondOrderId, secondProductId, 1, BigDecimal.valueOf(120), "ACTIVE");
    Review review = new Review(null, secondOrderItemId, buyerId, secondProductId, 5, null, "Great board");

    Review actual = reviewDao.save(review);

    assertAll(
            () -> assertNotNull(actual.id()),
            () -> assertEquals(secondOrderItemId, actual.orderItemId()),
            () -> assertEquals(buyerId, actual.userId()),
            () -> assertEquals(secondProductId, actual.productId()),
            () -> assertEquals(5, actual.rating()),
            () -> assertNotNull(actual.createdAt()),
            () -> assertEquals("Great board", actual.description())
    );
  }

  @Test
  void testUpdatePositive() {
    Review review = new Review(reviewId, orderItemId, buyerId, productId, 2, null, "Changed my mind");

    Optional<Review> actual = reviewDao.update(review);

    assertTrue(actual.isPresent());
    assertEquals(2, actual.get().rating());
    assertEquals("Changed my mind", actual.get().description());
  }

  @Test
  void testUpdateNegative() {
    Review review = new Review(Long.MAX_VALUE, orderItemId, buyerId, productId, 2, null, "Changed my mind");

    Optional<Review> actual = reviewDao.update(review);

    assertTrue(actual.isEmpty());
  }

  @Test
  void testDeletePositive() {
    boolean actual = reviewDao.delete(reviewId);
    List<Review> afterDelete = reviewDao.findAllByUser(buyerId);

    assertTrue(actual);
    assertTrue(afterDelete.isEmpty());
  }

  @Test
  void testDeleteNegative() {
    boolean actual = reviewDao.delete(Long.MAX_VALUE);

    assertFalse(actual);
  }

  @Test
  void testFindAllByUser() {
    Long secondProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Board", "M3", BigDecimal.valueOf(120), 5);
    Long secondOrderId = insertAndGetId("INSERT INTO orders (buyer_id, status, created_at) VALUES (?, ?, ?) RETURNING id",
            buyerId, "NEW", OffsetDateTime.now(ZoneOffset.UTC));
    Long secondOrderItemId = insertAndGetId("INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, status)" +
                    " VALUES (?, ?, ?, ?, ?) RETURNING id",
            secondOrderId, secondProductId, 1, BigDecimal.valueOf(120), "ACTIVE");
    Long secondReviewId = insertAndGetId("INSERT INTO reviews (order_item_id, user_id, product_id, rating, created_at, description)" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            secondOrderItemId, buyerId, secondProductId, 5, OffsetDateTime.now(ZoneOffset.UTC), "Great board");

    List<Review> actual = reviewDao.findAllByUser(buyerId);

    assertEquals(2, actual.size());
    assertEquals(secondReviewId, actual.get(0).id());
    assertEquals(reviewId, actual.get(1).id());
  }

  @Test
  void testFindAllByUserExcludesOtherUsers() {
    createAdditionalReview(5);

    List<Review> actual = reviewDao.findAllByUser(buyerId);

    assertEquals(1, actual.size());
    assertEquals(reviewId, actual.getFirst().id());
  }

  @Test
  void testFindAllByProductPagination() {
    Long higherRatedId = createAdditionalReview(5);

    List<Review> actual1 = reviewDao.findAllByProduct(productId, 1, 1);
    List<Review> actual2 = reviewDao.findAllByProduct(productId, 2, 1);

    assertEquals(1, actual1.size());
    assertEquals(higherRatedId, actual1.getFirst().id());
    assertEquals(1, actual2.size());
    assertEquals(reviewId, actual2.getFirst().id());
  }

  @Test
  void testCalculateAverageRatingPositive() {
    createAdditionalReview(2);

    Optional<BigDecimal> actual = reviewDao.calculateAverageRating(productId);

    assertTrue(actual.isPresent());
    assertEquals(0, BigDecimal.valueOf(3.0).compareTo(actual.get()));
  }

  @Test
  void testCalculateAverageRatingNegative() {
    Long unratedProductId = insertAndGetId("INSERT INTO products (seller_id, category_id, name, description, price, \"left\")" +
                    " VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
            sellerId, categoryId, "Board", "M3", BigDecimal.valueOf(120), 5);

    Optional<BigDecimal> actual = reviewDao.calculateAverageRating(unratedProductId);

    assertTrue(actual.isEmpty());
  }
}
