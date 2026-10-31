plugins {
    id("spring-boot-service-convention")
}

dependencies {
    implementation(projects.common.core)
    implementation(projects.common.web)
    implementation("org.springframework.boot:spring-boot-starter-web")
}
