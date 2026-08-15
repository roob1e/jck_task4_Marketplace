package by.assxmblxr.marketplace.model;

/**
 * Lifecycle status of a single {@link OrderItem} within an order.
 * Distinct from {@link OrderStatus}: an individual line item can be cancelled
 * (e.g. the seller ran out of stock) without cancelling the whole order.
 * Mirrors the {@code chk_order_items_status} check constraint on the
 * {@code order_items.status} column.
 */
public enum OrderItemStatus {
  /** Item is part of the order and expected to be fulfilled. */
  ACTIVE,
  /** Item was cancelled and will not be fulfilled. */
  CANCELLED
}
