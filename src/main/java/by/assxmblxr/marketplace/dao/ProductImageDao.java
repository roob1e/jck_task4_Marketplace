package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.ProductImage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductImageDao {
  ProductImage save(ProductImage productImage);
  Optional<ProductImage> update(ProductImage productImage);
  boolean delete(Long id);
  Optional<ProductImage> findById(Long id);
  Optional<ProductImage> findMainPhoto(Long productId);
  List<ProductImage> findAllByProductId(Long productId);
  Map<Long, ProductImage> findAllMainPhotos(List<Long> productIds);
}