import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Base convention for every Kotlin module: JDK 21 toolchain, compiler options,
// quality gates (detekt, ktlint), the shared test stack and a JaCoCo report.

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    jacoco
}

val libs = libraries

kotlin {
    // JDK 21: the LTS a reviewer most likely already has installed.
    jvmToolchain(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootDir.resolve("config/detekt/detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "21"
}

ktlint {
    version.set("1.8.0")
}

testing {
    suites {
        getByName<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation(platform("org.junit:junit-bom:${libs.findVersion("junit").get().requiredVersion}"))
                implementation(libs.findLibrary("kotest-assertions-core").get())
            }
        }
    }
}

// Coverage is reported, not gated: a codebase this size gains nothing from
// a floor, and a floor invites tests written to move a number.
tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestReport"))
}
