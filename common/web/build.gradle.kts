plugins {
    id("spring-boot-service-convention")
}

dependencies {
    api(projects.common.core)
    implementation("org.springframework.boot:spring-boot-starter-web")
}
