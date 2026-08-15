package by.assxmblxr.marketplace.model;

/**
 * Domain entity mapped to a row in the {@code categories} table.
 * Represents a product category used to group listings in the catalog.
 *
 * @param id   the primary key, {@code null} for an entity not yet persisted
 * @param name the category's display name, unique across all categories
 */
public record Category(
        Long id,
        String name
) {
}