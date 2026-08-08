package by.assxmblxr.marketplace.model;

public record User(
        Long id,
        Role role,
        String login,
        String passwordHash,
        String address
) {
}