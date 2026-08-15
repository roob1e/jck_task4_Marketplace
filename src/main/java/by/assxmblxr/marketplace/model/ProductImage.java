package by.assxmblxr.marketplace.model;

/**
 * Domain entity mapped to a row in the {@code product_images} table.
 * Represents a single image uploaded for a {@link Product}.
 *
 * @param id        the primary key, {@code null} for an entity not yet persisted
 * @param productId the id of the {@link Product} this image belongs to
 * @param path      the {@code FileStorage} key of the uploaded image (not a filesystem path,
 *                  despite the name)
 * @param order     the 1-based display position among the product's images;
 *                  {@code order == 1} identifies the product's main photo
 */
public record ProductImage(
        Long id,
        Long productId,
        String path,
        int order
) {
}