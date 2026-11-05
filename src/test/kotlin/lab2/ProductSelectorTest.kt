package lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductSelectorTest {
    @Test
    fun returnsEmptyListForZeroLimit() {
        val products = listOf(Product(1, "Keyboard", true))
        val recommendedIds = CountingList(listOf(1L, 2L, 3L))
        val selector = ProductSelector(products)

        val result = selector.select(recommendedIds, emptyList(), limit = 0)

        assertEquals(emptyList<Product>(), result)
        assertEquals(0, recommendedIds.viewedCount)
    }

    @Test
    fun stopsReadingRecommendationsWhenLimitIsReached() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)
        val selector = ProductSelector(products)

        // 99 неизвестен, 2 недоступен, 3 подходит
        val recommendedIds = CountingList(listOf(99L, 2L, 3L, 1L, 4L))

        val result = selector.select(recommendedIds, emptyList(), limit = 1)

        assertEquals(listOf(monitor), result)

        // после третьей рекомендации результат заполнен
        assertEquals(3, recommendedIds.viewedCount)
    }

    @Test
    fun returnsAllMatchingProductsWhenThereAreFewerThanLimit() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val products = listOf(keyboard, mouse, monitor)
        val selector = ProductSelector(products)

        val recommendedIds = CountingList(listOf(3L, 2L, 3L, 99L, 1L))
        val excludedIds = listOf(1L)

        val result = selector.select(recommendedIds, excludedIds, limit = 10)

        assertEquals(listOf(monitor), result)

        // лимит не достигнут, просмотрены все рекомендации
        assertEquals(5, recommendedIds.viewedCount)
    }

    @Test
    fun selectsProductsInRecommendationOrderAndSkipsInvalidProducts() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)

        // порядок каталога отличается от порядка рекомендаций
        val products = listOf(webcam, keyboard, mouse, monitor)
        val selector = ProductSelector(products)

        val recommendedIds = listOf(3L, 2L, 3L, 99L, 1L, 4L, 1L)
        val excludedIds = listOf(4L, 4L, 100L)

        val result = selector.select(recommendedIds, excludedIds, limit = 2)

        assertEquals(listOf(monitor, keyboard), result)
    }

    @Test
    fun doesNotMutateInputsAndKeepsRequestsIndependent() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)

        val products = mutableListOf(keyboard, monitor)
        val recommendedIds = mutableListOf(1L, 3L, 1L)
        val excludedIds = mutableListOf(3L)

        // сохраняем копии для проверки
        val productsCopy = products.toList()
        val recommendedCopy = recommendedIds.toList()
        val excludedCopy = excludedIds.toList()

        val selector = ProductSelector(products)

        val firstResult = selector.select(recommendedIds, excludedIds, limit = 10)
        val secondResult = selector.select(recommendedIds, emptyList(), limit = 10)

        assertEquals(listOf(keyboard), firstResult)

        // исключения и просмотренные id первого запроса не сохраняются
        assertEquals(listOf(keyboard, monitor), secondResult)

        // второй запрос не изменил первый результат
        assertEquals(listOf(keyboard), firstResult)

        assertEquals(productsCopy, products)
        assertEquals(recommendedCopy, recommendedIds)
        assertEquals(excludedCopy, excludedIds)
    }

    @Test
    fun usesNewCatalogAfterReload() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", true)
        val selector = ProductSelector(listOf(keyboard, mouse))

        val recommendedIds = listOf(1L, 2L, 3L)

        val firstResult = selector.select(recommendedIds, emptyList(), limit = 10)

        assertEquals(listOf(keyboard, mouse), firstResult)

        // обновляем товар 1, удаляем товар 2 и добавляем товар 3
        val updatedKeyboard = Product(1, "Updated Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        selector.loadCatalog(listOf(updatedKeyboard, monitor))

        val secondResult = selector.select(recommendedIds, emptyList(), limit = 10)

        assertEquals(listOf(updatedKeyboard, monitor), secondResult)

        // смена каталога не меняет уже полученный результат
        assertEquals(listOf(keyboard, mouse), firstResult)
    }

    @Test
    fun returnsEmptyListForEmptyCatalogOrEmptyRecommendations() {
        val emptySelector = ProductSelector(emptyList())

        assertEquals(
            emptyList<Product>(),
            emptySelector.select(listOf(1L, 2L), emptyList(), limit = 10)
        )

        val selector = ProductSelector(listOf(Product(1, "Keyboard", true)))

        assertEquals(
            emptyList<Product>(),
            selector.select(emptyList(), emptyList(), limit = 10)
        )
    }

    // AbstractList использует get при обходе стандартным итератором
    private class CountingList(private val ids: List<Long>) : AbstractList<Long>() {
        var viewedCount = 0
            private set

        override val size: Int
            get() = ids.size

        override fun get(index: Int): Long {
            val id = ids[index]
            viewedCount++
            return id
        }
    }
}
