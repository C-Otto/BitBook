import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    id("java")
    id("com.adarshr.test-logger")
}

testing {
    suites {
        named("test") {
            dependencies {
                testImplementation("nl.jqno.equalsverifier:equalsverifier:3.10")
            }
        }
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.awaitility:awaitility:4.2.0")
            }
        }
    }
}


tasks.withType<Test>().configureEach {
    afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ _, result ->
        if (result.resultType == TestResult.ResultType.SKIPPED) {
            throw GradleException("Do not ignore test cases")
        }
    }))
    systemProperties = mapOf("junit.jupiter.displayname.generator.default" to "org.junit.jupiter.api.DisplayNameGenerator\$ReplaceUnderscores")
}

testlogger {
    theme = ThemeType.STANDARD_PARALLEL
    slowThreshold = 1000
    showSimpleNames = true
    showOnlySlow = true
}
