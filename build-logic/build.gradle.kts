plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.toPluginDependency())
    implementation(libs.plugins.kotlin.spring.toPluginDependency())
    implementation(libs.plugins.spring.boot.toPluginDependency())
    implementation(libs.plugins.detekt.toPluginDependency())
    implementation(libs.plugins.ktlint.toPluginDependency())
}

// Precompiled script plugins can't `alias(libs.plugins.x)` directly; this turns a
// catalog plugin entry into its marker-artifact coordinate so build-logic's tool
// versions still come from the one shared libs.versions.toml.
fun Provider<org.gradle.plugin.use.PluginDependency>.toPluginDependency() =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
