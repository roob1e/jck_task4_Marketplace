package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.Review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewDao {
  Review save(Review review);
  Optional<Review> update(Review review);
  boolean delete(Long id);
  List<Review> findAllByUser(Long userId);
  List<Review> findAllByProduct(Long productId, int page, int pageSize);
  Optional<BigDecimal> calculateAverageRating(Long productId);
}