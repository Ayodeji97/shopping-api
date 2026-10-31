// Convention for a Spring-managed library module that is not itself bootable
// (everything under common/* and modules/*): Kotlin-Spring interop, the Spring
// Boot BOM, and coroutines, since every use case in this API is a suspend function.

plugins {
    id("kotlin-common-convention")
    id("org.jetbrains.kotlin.plugin.spring")
}

val springBootVersion = libraries.findVersion("springBoot").get().requiredVersion
val springBootBom = "org.springframework.boot:spring-boot-dependencies:$springBootVersion"

dependencies {
    implementation(platform(springBootBom))
    testImplementation(platform(springBootBom))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
