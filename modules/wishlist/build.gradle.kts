plugins {
    id("spring-boot-service-convention")
}

dependencies {
    implementation(projects.common.core)
    implementation(projects.common.web)
    // Only catalog's `api` package is visible here: everything else in catalog is `internal`.
    implementation(projects.modules.catalog)
    implementation("org.springframework.boot:spring-boot-starter-web")
}
