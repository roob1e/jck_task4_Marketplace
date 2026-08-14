package by.assxmblxr.marketplace.dao;

import by.assxmblxr.marketplace.model.Order;
import by.assxmblxr.marketplace.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
  Order save(Order order);
  boolean updateStatus(Long id, OrderStatus orderStatus);
  Optional<Order> findById(Long id);
  List<Order> findAllByBuyer(Long buyerId, int page, int pageSize);
}
