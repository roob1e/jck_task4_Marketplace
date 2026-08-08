package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
  Product save(Product product);
  Optional<Product> update(Product product);
  boolean deactivate(Long id);
  Optional<Product> findById(Long id);
  List<Product> findAllActive(int page, int pageSize);
  List<Product> findAllBySeller(Long sellerId, int page, int pageSize);
}
