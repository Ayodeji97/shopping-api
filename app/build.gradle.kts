plugins {
    id("spring-boot-app-convention")
}

dependencies {
    // The composition root: depends on every module so their beans are on the classpath.
    implementation(projects.common.core)
    implementation(projects.common.web)
    implementation(projects.modules.catalog)
    implementation(projects.modules.wishlist)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Jackson 3's Kotlin module: lets data classes be serialised with their Kotlin semantics.
    implementation("tools.jackson.module:jackson-module-kotlin")
    // Spring MVC runs `suspend` controller methods through Reactor's coroutine bridge.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // Swagger UI at /swagger-ui.html and the OpenAPI document at /v3/api-docs.
    implementation(libs.springdoc.webmvc.ui)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(libs.konsist)
}

springBoot {
    mainClass.set("com.danzucker.shopping.app.ShoppingApplicationKt")
}

// Konsist reads every module's source straight off disk, which Gradle cannot infer, so the
// sources are declared as test inputs. Without this a rule-breaking change elsewhere would leave
// `:app:test` UP-TO-DATE and the architecture rules would not run.
tasks.named<Test>("test") {
    inputs
        .files(
            fileTree(rootDir) {
                include("**/src/*/kotlin/**/*.kt")
                exclude("**/build/**")
            },
        ).withPropertyName("konsistScannedSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
