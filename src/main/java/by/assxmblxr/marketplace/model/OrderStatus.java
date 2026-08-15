package by.assxmblxr.marketplace.model;

/**
 * Lifecycle status of an {@link Order} as a whole.
 * Mirrors the {@code chk_orders_status} check constraint on the {@code orders.status} column.
 */
public enum OrderStatus {
  /** Order has been placed but not yet processed by the seller. */
  NEW,
  /** Order is being prepared/processed by the seller. */
  PROCESSING,
  /** Order has been handed off for delivery. */
  SHIPPED,
  /** Order has been delivered to the buyer. */
  DELIVERED,
  /** Order was cancelled and will not be fulfilled. */
  CANCELLED
}