package lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProductSelectionTest {
    @Test
    fun selectsAvailableProductsInRecommendationOrder() {
        val keyboard = Product(1, "Keyboard", true)
        val mouse = Product(2, "Mouse", false)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)
        val products = listOf(keyboard, mouse, monitor, webcam)

        val result = selectProducts(
            products,
            listOf(3L, 2L, 3L, 99L, 1L, 4L),
            listOf(1L)
        )

        assertEquals(listOf(monitor, webcam), result)
    }

    @Test
    fun returnsEmptyListWhenThereAreNoRecommendations() {
        val products = listOf(Product(1, "Keyboard", true))

        val result = selectProducts(products, emptyList(), emptyList())

        assertEquals(emptyList<Product>(), result)
    }

    @Test
    fun selectsProductsInRecommendationOrderRegardlessOfCatalogOrder() {
        val keyboard = Product(1, "Keyboard", true)
        val monitor = Product(3, "Monitor", true)
        val webcam = Product(4, "Webcam", true)

        val products = listOf(webcam, keyboard, monitor)
        val recommendedIds = listOf(3L, 1, 4)

        val result = selectProducts(products, recommendedIds, emptyList())

        assertEquals(listOf(monitor, keyboard, webcam), result)
    }

    @Test
    fun handlesDuplicateRecommendationsAndExclusions() {
        val p1 = Product(1, "A", true)
        val p2 = Product(2, "B", true)
        val p3 = Product(3, "C", true)
        val products = listOf(p1, p2, p3)

        // дубли в рекомендациях и исключениях
        val recommendedIds = listOf(1L, 1L, 2L, 3L, 1L)
        val excludedIds = listOf(2L, 2L, 99L) // 2 исключен, 99 не существует

        val result = selectProducts(products, recommendedIds, excludedIds)

        assertEquals(listOf(p1, p3), result)
    }

    @Test
    fun skipsUnavailableExcludedAndUnknownProducts() {
        val p1 = Product(1, "A", true)
        val p2 = Product(2, "B", false)
        val p3 = Product(3, "C", true)
        val p4 = Product(4, "D", true)
        val products = listOf(p1, p2, p3, p4)

        val recommendedIds = listOf(1L, 2L, 3L, 4L, 99L) // 99 отсутствует в каталоге
        val excludedIds = listOf(4L) // 4 исключен

        val result = selectProducts(products, recommendedIds, excludedIds)

        assertEquals(listOf(p1, p3), result)
    }

    @Test
    fun returnsEmptyListForEmptyCatalogOrEmptyExclusions() {
        // пустой каталог
        assertTrue(selectProducts(emptyList(), listOf(1L, 2L), emptyList()).isEmpty())

        // пустые исключения (должен вернуть товары)
        val products = listOf(Product(1, "A", true))
        assertEquals(products, selectProducts(products, listOf(1L), emptyList()))
    }

    @Test
    fun returnsEmptyListWhenNoProductsMatch() {
        val p1 = Product(1, "A", false) // недоступен
        val p2 = Product(2, "B", true) // исключен
        val products = listOf(p1, p2)

        val recommendedIds = listOf(1L, 2L, 99L)
        val excludedIds = listOf(2L)

        assertTrue(selectProducts(products, recommendedIds, excludedIds).isEmpty())
    }

    @Test
    fun doesNotMutateInputsAndIsIdempotent() {
        val products = mutableListOf(Product(1, "A", true), Product(2, "B", true))
        val recommendedIds = mutableListOf(1L, 2L, 1L)
        val excludedIds = mutableListOf(2L)

        // сохраняем копии для проверки
        val productsCopy = products.toList()
        val recommendedCopy = recommendedIds.toList()
        val excludedCopy = excludedIds.toList()

        val firstResult = selectProducts(products, recommendedIds, excludedIds)
        val secondResult = selectProducts(products, recommendedIds, excludedIds)

        // 1. проверяем, что входные данные не изменились
        assertEquals(productsCopy, products)
        assertEquals(recommendedCopy, recommendedIds)
        assertEquals(excludedCopy, excludedIds)

        // 2. проверяем, что повторный вызов дает тот же результат
        assertEquals(firstResult, secondResult)

        // проверяем, что результат содержит только p1
        assertEquals(listOf(Product(1, "A", true)), firstResult)
    }
}
