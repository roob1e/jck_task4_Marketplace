package by.assxmblxr.marketplace.model;

/**
 * Domain entity mapped to a row in the {@code users} table.
 * Represents an account that can act as a buyer and/or seller in the marketplace.
 *
 * @param id           the primary key, {@code null} for an entity not yet persisted
 * @param userRole         the account's userRole, determining what actions it is permitted to perform
 * @param login        the unique login name used to authenticate
 * @param passwordHash the hashed password; the plaintext password is never stored
 * @param address      the user's postal address, {@code null} if not provided
 */
public record User(
        Long id,
        UserRole userRole,
        String login,
        String passwordHash,
        String address
) {
}