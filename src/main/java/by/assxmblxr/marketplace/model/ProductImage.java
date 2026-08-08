package by.assxmblxr.marketplace.model;

public record ProductImage(
        Long id,
        Long productId,
        String path,
        int order
) {
}