package com.danzucker.shopping.app

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * The Clean Architecture and module-boundary rules, as executable checks. They read the source
 * of every module, so a change that breaks a rule fails the build instead of waiting for review.
 */
internal class ArchitectureTest {
    private val production = Konsist.scopeFromProduction()

    private fun layer(name: String) = production.files.filter { it.packagee?.name?.endsWith(".$name") == true }

    @Test
    fun `domain and use cases are framework-free plain Kotlin`() {
        (layer("domain") + layer("usecase")).assertFalse { file ->
            file.imports.any { it.name.startsWith("org.springframework") }
        }
    }

    @Test
    fun `the domain depends on no outer layer`() {
        layer("domain").assertFalse { file ->
            file.imports.any { it.name.contains(Regex("\\.(usecase|data|presentation)\\.")) }
        }
    }

    @Test
    fun `use cases do not depend on data sources or controllers`() {
        layer("usecase").assertFalse { file ->
            file.imports.any { it.name.contains(Regex("\\.(data|presentation)\\.")) }
        }
    }

    @Test
    fun `controllers live only in a presentation layer`() {
        production.classes().filter { it.hasAnnotationWithName("RestController") }.assertTrue { cls ->
            cls.resideInPackage("..presentation..")
        }
    }

    @Test
    fun `the wishlist module reaches the catalog only through its public api`() {
        production.files
            .filter { it.packagee?.name?.startsWith("com.danzucker.shopping.wishlist") == true }
            .assertFalse { file ->
                file.imports.any {
                    it.name.startsWith("com.danzucker.shopping.catalog.") &&
                        !it.name.startsWith("com.danzucker.shopping.catalog.api.")
                }
            }
    }
}
