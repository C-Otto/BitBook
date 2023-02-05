plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    api("io.github.resilience4j:resilience4j-spring-boot2")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("io.vavr:vavr")
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
