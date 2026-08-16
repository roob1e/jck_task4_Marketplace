package by.assxmblxr.marketplace.service;

import by.assxmblxr.marketplace.model.User;

/**
 * Result of a successful {@link UserService#login(String, String, boolean)} call.
 *
 * @param user  the authenticated user
 * @param token the raw remember-me token to set as a cookie, or {@code null} if the
 *              caller did not request one. This is the only place the raw value is
 *              ever exposed — everywhere else, only its hash exists.
 */
public record LoginResult(
        User user,
        String token
) {
}
