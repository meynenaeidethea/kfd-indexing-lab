package lab2

import kotlin.math.min

fun selectProducts(
    products: List<Product>,
    recommendedIds: List<Long>,
    excludedIds: List<Long>
): List<Product> {
    val productById = products.associateBy { it.id }
    val excludedSet = excludedIds.toHashSet()

    val capacity = min(recommendedIds.size, products.size)
    val addedIds = HashSet<Long>(capacity)
    val result = ArrayList<Product>(capacity)

    for (id in recommendedIds) {
        // repeated recommendations
        if (!addedIds.add(id)) continue
        // excluded ids
        if (excludedSet.contains(id)) continue
        // find in catalog
        val product = productById[id] ?: continue
        // is available?
        if (product.available) {
            result.add(product)
        }
    }

    return result
}
