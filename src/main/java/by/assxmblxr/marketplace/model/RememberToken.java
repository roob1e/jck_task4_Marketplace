package by.assxmblxr.marketplace.model;

import java.time.OffsetDateTime;

/**
 * Domain entity mapped to a row in the {@code remember_tokens} table.
 * Backs "remember me" login: a long-lived token issued to one device on successful
 * authentication, checked on requests where the session is missing, and extended
 * (sliding expiration) on each successful use so an active device never needs to
 * log in again while a fresh token remains inside {@link #expiresAt()}.
 * <p>
 * {@link #tokenHash()} is the primary key and must never hold the raw token value:
 * only its hash is persisted, so that a database leak alone cannot be used to
 * impersonate a user.
 *
 * @param tokenHash hash of the raw token sent to the client; unique, identifies the row
 * @param userId    the {@link User} this token authenticates
 * @param expiresAt the point in time after which this token is no longer valid
 */
public record RememberToken(
        String tokenHash,
        Long userId,
        OffsetDateTime expiresAt
) {
}