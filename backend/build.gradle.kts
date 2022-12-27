plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    api("io.github.resilience4j:resilience4j-spring-boot2:1.7.1")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(project(":backend:models"))
    runtimeOnly("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    runtimeOnly("com.h2database:h2")
    testImplementation(testFixtures(project(":backend:models")))
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "CLASS") {
                    limit.minimum = 0.8.toBigDecimal()
                }
            }
        }
    }
}
