package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.OrderItem;
import by.assxmblxr.marketplace.model.OrderItemStatus;

import java.util.List;
import java.util.Optional;

public interface OrderItemDao {
  OrderItem save(OrderItem orderItem);
  boolean updateStatus(Long id, OrderItemStatus orderItemStatus);
  Optional<OrderItem> findById(Long id);
  List<OrderItem> findAllByOrder(Long orderId);
  List<OrderItem> findAllBySeller(Long sellerId, int page, int pageSize);
}