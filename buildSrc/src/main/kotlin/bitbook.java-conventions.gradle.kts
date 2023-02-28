plugins {
    id("de.c-otto.java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java-test-fixtures")
}

dependencies {
    implementation(platform("de.c-otto.bitbook:platform"))
    testFixturesImplementation(platform("de.c-otto.bitbook:platform"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.google.guava:guava")
}

testing {
    suites {
        named("integrationTest", JvmTestSuite::class).configure {
            dependencies {
                implementation("com.tngtech.archunit:archunit")
            }
        }
        withType<JvmTestSuite>().configureEach {
            dependencies {
                implementation(project.dependencies.platform("de.c-otto.bitbook:platform"))
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.awaitility:awaitility")
            }
        }
    }
}

configurations.named("testRuntimeOnly") {
    exclude(group = "ch.qos.logback", module = "logback-classic")
}
