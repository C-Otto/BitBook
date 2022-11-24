plugins {
    id("bitbook.tests")
}

testing {
    suites {
        this.register("integrationTest", JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            dependencies {
                implementation(project)
                implementation("com.tngtech.archunit:archunit:1.0.1")
            }

            targets {
                configureEach {
                    testTask.configure {
                        shouldRunAfter(testing.suites.named("test"))
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

testing.suites.named("integrationTest") {
    testlogger {
        slowThreshold = 2000
    }
}
