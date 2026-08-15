package by.assxmblxr.marketplace.model;

/**
 * Role of a {@link User} account, determining what actions it is permitted to perform.
 * Names must match the {@code roles.name} rows in the database exactly, since DAO
 * implementations resolve a user's {@code role_id} by looking up a role by this name.
 */
public enum Role {
  /** Can browse products and place orders. */
  BUYER,
  /** Can list products for sale. */
  SELLER,
}