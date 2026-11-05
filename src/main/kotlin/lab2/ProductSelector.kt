package lab2

class ProductSelector(products: List<Product>) {
    private var productById: Map<Long, Product> = products.associateBy { it.id }

    fun loadCatalog(products: List<Product>) {
        productById = products.associateBy { it.id }
    }

    fun select(recommendedIds: List<Long>, excludedIds: List<Long>, limit: Int): List<Product> {
        if (limit == 0) return emptyList()

        val excludedSet = excludedIds.toHashSet()

        val capacity = minOf(recommendedIds.size, productById.size, limit)
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
                // limit reached
                if (result.size == limit) break
            }
        }

        return result
    }
}