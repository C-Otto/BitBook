plugins {
    id("bitbook.java-library-conventions")
}

dependencies {
    implementation(project(":backend:models"))
    implementation(project(":backend:address-transactions"))
    implementation(project(":backend:provider:base"))
    implementation("org.apache.mina:mina-core")
    testImplementation(testFixtures(project(":backend:models")))
    testFixturesImplementation(testFixtures(project(":backend:address-transactions")))
    testFixturesImplementation(testFixtures(project(":backend:models")))
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rules.forEach {rule ->
            rule.limits.forEach {limit ->
                if (limit.counter == "BRANCH") {
                    limit.minimum = 0.94.toBigDecimal()
                }
                if (limit.counter == "INSTRUCTION") {
                    limit.minimum = 0.91.toBigDecimal()
                }
            }
        }
    }
}

pitest {
    testStrengthThreshold.set(93)
}
